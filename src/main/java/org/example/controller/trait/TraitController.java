package org.example.controller.trait;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.trait.TraitFilterRequest;
import org.example.dto.trait.TraitResponse;
import org.example.services.implement.TraitServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/traits")
@RequiredArgsConstructor
@Slf4j
public class TraitController {

    private final TraitServiceImpl traitService;

    @GetMapping
    public ApiResponse<PageResponse<TraitResponse>> getAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("[GET] /traits keyword={} page={} size={}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());
        return ApiResponse.success(traitService.getAll(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<TraitResponse> getById(@PathVariable Long id) {
        log.info("[GET] /traits/{}", id);
        return ApiResponse.success(traitService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<TraitResponse> getBySlug(@PathVariable String slug) {
        log.info("[GET] /traits/slug/{}", slug);
        return ApiResponse.success(traitService.getBySlug(slug));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<TraitResponse>> search(
            @ModelAttribute TraitFilterRequest filter,
            Pageable pageable) {
        log.info("[GET] /traits/search filter={}", filter);
        return ApiResponse.success(traitService.search(filter, pageable));
    }

    @GetMapping("/sorted")
    public ApiResponse<List<TraitResponse>> getAllSortedByNameAsc(
            @RequestParam(required = false) Long setId) {
        log.info("[GET] /traits/sorted setId={}", setId);
        return ApiResponse.success(traitService.getAllSortedByNameAsc(setId));
    }

    @GetMapping("/set/{setId}")
    public ApiResponse<List<TraitResponse>> getBySetId(@PathVariable Long setId) {
        log.info("[GET] /traits/set/{}", setId);
        return ApiResponse.success(traitService.getBySetId(setId));
    }

    @GetMapping("/dropdown")
    public ApiResponse<List<TraitResponse>> getForDropdown(
            @RequestParam(required = false) Long setId) {
        log.info("[GET] /traits/dropdown setId={}", setId);
        return ApiResponse.success(traitService.getForDropdown(setId));
    }
}
