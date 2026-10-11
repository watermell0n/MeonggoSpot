package com.ssafy.backend.post.dto;

import java.time.Instant;

public record UpdatePostResponse(long postId, long version, Instant updatedAt) {}
