package com.ssafy.backend.auth.service;

import com.ssafy.backend.auth.dto.PhoneVerificationResponse;
import com.ssafy.backend.auth.exception.AuthErrorCode;
import com.ssafy.backend.auth.exception.InputValidationException;
import com.ssafy.backend.auth.exception.PhoneErrorCode;
import com.ssafy.backend.auth.exception.RetryableAuthException;
import com.ssafy.backend.auth.repository.PhoneVerificationStore;
import com.ssafy.backend.auth.repository.PhoneVerificationStore.OtpSnapshot;
import com.ssafy.backend.auth.repository.PhoneVerificationStore.ProofRecord;
import com.ssafy.backend.auth.repository.PhoneVerificationStore.ProofSnapshot;
import com.ssafy.backend.auth.security.PhoneProtection;
import com.ssafy.backend.auth.sms.SmsSender;
import com.ssafy.backend.global.error.BusinessException;
import com.ssafy.backend.member.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class PhoneVerificationService {
    private final PhoneProtection protection;
    private final PhoneVerificationStore store;
    private final SmsSender sms;
    private final MemberRepository members;
    private final OtpGenerator generator;

    public PhoneVerificationService(
            PhoneProtection protection,
            PhoneVerificationStore store,
            SmsSender sms,
            MemberRepository members,
            OtpGenerator generator) {
        this.protection = protection;
        this.store = store;
        this.sms = sms;
        this.members = members;
        this.generator = generator;
    }

    public void requestCode(
            String phoneNumber, byte[] clientIp, Boolean consentAgreed, String consentVersion) {
        PrivacyCollectionConsent.requireCurrent(consentAgreed, consentVersion);
        String phone = protection.normalize(phoneNumber);
        String phoneHash = protection.lookupHash(phone);
        String requestId = UUID.randomUUID().toString();
        long retryAfter = store.reserveSend(phoneHash, protection.ipHash(clientIp), requestId);
        if (retryAfter > 0) {
            throw new RetryableAuthException(PhoneErrorCode.RATE_LIMITED, retryAfter);
        }
        String code = generator.generate();
        String verificationId = UUID.randomUUID().toString();
        sms.send(phone, code);
        if (!store.activateOtp(
                phoneHash, requestId, verificationId, protection.otpHash(verificationId, code))) {
            throw new RetryableAuthException(PhoneErrorCode.SMS_UNAVAILABLE, 60);
        }
    }

    public PhoneVerificationResponse confirm(
            String phoneNumber, String code, Boolean consentAgreed, String consentVersion) {
        PrivacyCollectionConsent.requireCurrent(consentAgreed, consentVersion);
        String phoneHash = protection.lookupHash(protection.normalize(phoneNumber));
        if (code == null || !code.matches("[0-9]{6}")) {
            throw new InputValidationException("verificationCode", "인증 코드는 숫자 6자리여야 합니다.");
        }
        OtpSnapshot otp = store.findOtp(phoneHash).orElseThrow(PhoneProof::invalid);
        boolean matches =
                constantEquals(otp.hash(), protection.otpHash(otp.verificationId(), code));
        PhoneProof proof = PhoneProof.generate();
        Instant issuedAt = Instant.now();
        ProofRecord record =
                new ProofRecord(
                        proof.selector(),
                        proof.secretHash(),
                        phoneHash,
                        issuedAt,
                        issuedAt.plusSeconds(600));
        if (!store.confirmOtp(phoneHash, otp.verificationId(), otp.hash(), matches, record)) {
            throw PhoneProof.invalid();
        }
        try {
            if (members.existsByPhoneLookupHash(phoneHash)) {
                throw new BusinessException(PhoneErrorCode.PHONE_IN_USE);
            }
        } catch (DataAccessException ex) {
            throw new RetryableAuthException(AuthErrorCode.AUTH_UNAVAILABLE, 1);
        }
        ProofSnapshot stored = store.findProof(proof.selector()).orElseThrow(PhoneProof::invalid);
        return new PhoneVerificationResponse(proof.token(), stored.expiresAt());
    }

    static boolean constantEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    @FunctionalInterface
    public interface OtpGenerator {
        String generate();

        static OtpGenerator random() {
            SecureRandom random = new SecureRandom();
            return () -> String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        }
    }
}
