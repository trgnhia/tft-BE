package org.example.controller.champ;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.champs.ChampResponse;
import org.example.services.ChampService;
import org.example.services.implement.ChampServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/champs")
@RequiredArgsConstructor
@Slf4j
public class ChampController {

    private final ChampServiceImpl champService;

    @GetMapping
    public ApiResponse<PageResponse<ChampResponse>> getAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("REST request to get all Champs with keyword: {}", keyword);
        return ApiResponse.success(champService.getAll(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ChampResponse> getById(@PathVariable Long id) {
        log.info("REST request to get Champ by id: {}", id);
        return ApiResponse.success(champService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<ChampResponse> getBySlug(@PathVariable String slug) {
        log.info("REST request to get Champ by slug: {}", slug);
        return ApiResponse.success(champService.getBySlug(slug));
    }

    @GetMapping("/set/{setId}")
    public ApiResponse<List<ChampResponse>> getBySetId(@PathVariable Long setId) {
        log.info("REST get champs by setId={}", setId);
        return ApiResponse.success(champService.getBySetId(setId));
    }
}