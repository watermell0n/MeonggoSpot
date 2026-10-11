package com.ssafy.backend.post.dto;

import com.ssafy.backend.post.entity.CloseReason;
import java.time.Instant;

public record PostClosureResponse(
        long postId, String status, long version, CloseReason closeReason, Instant closedAt) {}
