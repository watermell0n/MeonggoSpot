package com.ssafy.backend.post.controller;

import com.ssafy.backend.auth.security.AuthPrincipal;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.post.dto.PostPhotoReplacementResponse;
import com.ssafy.backend.post.service.PostMutationInput;
import com.ssafy.backend.post.service.PostPhotoReplacementService;
import com.ssafy.backend.post.web.PostMultipartReader;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostPhotoReplacementController {
    private final PostMultipartReader reader;
    private final PostPhotoReplacementService posts;

    public PostPhotoReplacementController(
            PostMultipartReader reader, PostPhotoReplacementService posts) {
        this.reader = reader;
        this.posts = posts;
    }

    @PutMapping(
            value = "/api/v1/posts/{postId}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostPhotoReplacementResponse> replace(
            @PathVariable long postId,
            @AuthenticationPrincipal AuthPrincipal principal,
            HttpServletRequest request) {
        var input = reader.read(request);
        PostMutationInput.fields(input.metadata(), Set.of("version"));
        long version = PostMutationInput.version(input.metadata());
        return ApiResponse.success(
                "게시물 사진을 교체했습니다.",
                posts.replace(postId, principal.memberId(), version, input.photos()));
    }
}
