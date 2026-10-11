package com.ssafy.backend.ping.controller;

import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.ping.dto.PingResponse;
import com.ssafy.backend.ping.service.PingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ping")
public class PingController {

    private final PingService pingService;

    public PingController(PingService pingService) {
        this.pingService = pingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PingResponse>> ping() {
        return ResponseEntity.ok(ApiResponse.success(pingService.ping()));
    }
}
