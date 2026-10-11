package com.ssafy.backend.adoption.controller;

import com.ssafy.backend.adoption.dto.AdoptionFavoriteListResponse;
import com.ssafy.backend.adoption.dto.AdoptionListResponse;
import com.ssafy.backend.adoption.dto.AdoptionSwipeListResponse;
import com.ssafy.backend.adoption.query.AdoptionListInputPolicy;
import com.ssafy.backend.adoption.service.AdoptionFavoriteService;
import com.ssafy.backend.adoption.service.AdoptionListService;
import com.ssafy.backend.adoption.service.AdoptionSwipeService;
import com.ssafy.backend.auth.security.AuthPrincipal;
import com.ssafy.backend.global.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdoptionController {
    private final AdoptionListInputPolicy inputs;
    private final AdoptionListService adoptions;
    private final AdoptionFavoriteService favorites;
    private final AdoptionSwipeService swipes;

    public AdoptionController(
            AdoptionListInputPolicy inputs,
            AdoptionListService adoptions,
            AdoptionFavoriteService favorites,
            AdoptionSwipeService swipes) {
        this.inputs = inputs;
        this.adoptions = adoptions;
        this.favorites = favorites;
        this.swipes = swipes;
    }

    /** 인증이 필요하다 (2026-09-25). 넘긴 동물을 빼려면 누가 넘겼는지 알아야 한다. */
    @GetMapping("/api/v1/adoptions")
    public ResponseEntity<ApiResponse<AdoptionListResponse>> list(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam MultiValueMap<String, String> parameters) {
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(
                        ApiResponse.success(
                                "입양 후보 목록을 조회했습니다.",
                                adoptions.list(inputs.parse(parameters), principal.memberId())));
    }

    @GetMapping("/api/v1/members/me/adoption-favorites")
    public ResponseEntity<ApiResponse<AdoptionFavoriteListResponse>> favorites(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam MultiValueMap<String, String> parameters) {
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(
                        ApiResponse.success(
                                "입양 찜 목록을 조회했습니다.",
                                favorites.list(principal.memberId(), parameters)));
    }

    @PutMapping("/api/v1/members/me/adoption-favorites/{postId}")
    public ResponseEntity<Void> addFavorite(
            @PathVariable long postId, @AuthenticationPrincipal AuthPrincipal principal) {
        favorites.add(principal.memberId(), postId);
        return ResponseEntity.noContent().header("Cache-Control", "no-store").build();
    }

    @DeleteMapping("/api/v1/members/me/adoption-favorites/{postId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable long postId, @AuthenticationPrincipal AuthPrincipal principal) {
        favorites.remove(principal.memberId(), postId);
        return ResponseEntity.noContent().header("Cache-Control", "no-store").build();
    }

    /** 어느 쪽으로 넘겼는지는 받지 않는다 — 이 기록은 "이미 봤다" 만 뜻한다 (api-spec AD5). */
    @PutMapping("/api/v1/members/me/adoption-swipes/{postId}")
    public ResponseEntity<Void> recordSwipe(
            @PathVariable long postId, @AuthenticationPrincipal AuthPrincipal principal) {
        swipes.record(principal.memberId(), postId);
        return ResponseEntity.noContent().header("Cache-Control", "no-store").build();
    }

    @GetMapping("/api/v1/members/me/adoption-swipes")
    public ResponseEntity<ApiResponse<AdoptionSwipeListResponse>> swipes(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam MultiValueMap<String, String> parameters) {
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(
                        ApiResponse.success(
                                "넘긴 동물 목록을 조회했습니다.",
                                swipes.list(principal.memberId(), parameters)));
    }
}
