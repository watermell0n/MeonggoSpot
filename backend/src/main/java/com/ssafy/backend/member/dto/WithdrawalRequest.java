package com.ssafy.backend.member.dto;

public record WithdrawalRequest(String currentPassword) {
    @Override
    public String toString() {
        return "WithdrawalRequest[REDACTED]";
    }
}
