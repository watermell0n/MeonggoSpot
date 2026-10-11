package com.ssafy.backend.ping.service;

import com.ssafy.backend.ping.dto.PingResponse;
import org.springframework.stereotype.Service;

@Service
public class PingService {

    public PingResponse ping() {
        return PingResponse.pong();
    }
}
