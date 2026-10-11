package com.ssafy.backend.post.dto;

import com.ssafy.backend.post.entity.CaseType;
import java.time.Instant;

public record CreatePostResponse(
        long postId,
        CaseType type,
        String source,
        String status,
        long version,
        Instant createdAt) {}
