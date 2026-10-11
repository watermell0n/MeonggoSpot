package com.ssafy.backend.ping.dto;

public record PingResponse(String status) {

    public static PingResponse pong() {
        return new PingResponse("pong");
    }
}
