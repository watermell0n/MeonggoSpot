package com.ssafy.backend.post.controller;

import com.ssafy.backend.auth.security.AuthPrincipal;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.post.dto.PostClosureResponse;
import com.ssafy.backend.post.entity.CloseReason;
import com.ssafy.backend.post.service.PostClosureService;
import com.ssafy.backend.post.service.PostMutationInput;
import com.ssafy.backend.post.web.PostJsonReader;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostClosureController {
    private final PostJsonReader reader;
    private final PostClosureService posts;

    public PostClosureController(PostJsonReader reader, PostClosureService posts) {
        this.reader = reader;
        this.posts = posts;
    }

    @PostMapping(
            value = "/api/v1/posts/{postId}/closure",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PostClosureResponse> close(
            @PathVariable long postId,
            @AuthenticationPrincipal AuthPrincipal principal,
            HttpServletRequest request) {
        var input = reader.read(request);
        PostMutationInput.fields(input, Set.of("version", "reason"));
        long version = PostMutationInput.version(input);
        var reasonNode = input.get("reason");
        if (reasonNode == null || !reasonNode.isString()) throw PostMutationInput.invalid("reason");
        CloseReason reason;
        try {
            reason = CloseReason.valueOf(reasonNode.asString());
        } catch (IllegalArgumentException ex) {
            throw PostMutationInput.invalid("reason");
        }
        return ApiResponse.success(
                "게시물을 종료했습니다.", posts.close(postId, principal.memberId(), version, reason));
    }
}
