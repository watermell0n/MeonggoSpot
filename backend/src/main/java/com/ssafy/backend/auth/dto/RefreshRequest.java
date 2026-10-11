package com.ssafy.backend.auth.dto;

public record RefreshRequest(String refreshToken) {
    @Override
    public String toString() {
        return "RefreshRequest[REDACTED]";
    }
}
