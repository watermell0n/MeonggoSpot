package com.ssafy.backend.post.query;

import com.ssafy.backend.post.entity.CaseStatus;
import com.ssafy.backend.post.entity.CaseType;
import com.ssafy.backend.post.entity.Sex;
import com.ssafy.backend.post.entity.SourceType;
import com.ssafy.backend.post.entity.Species;

public record PostListQuery(
        CaseType type,
        Species species,
        Sex sex,
        String breedName,
        String regionCode,
        String color,
        SourceType source,
        CaseStatus status,
        PostListSort sort,
        String cursor) {
    @Override
    public String toString() {
        return "PostListQuery[redacted]";
    }
}
