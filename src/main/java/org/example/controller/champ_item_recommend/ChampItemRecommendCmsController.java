package org.example.controller.champ_item_recommend;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.champ_item_recommend.ChampItemRecommendRequest;
import org.example.dto.champ_item_recommend.ChampItemRecommendResponse;
import org.example.services.ChampItemRecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cms/champ-item-recommends")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
public class ChampItemRecommendCmsController {

    private final ChampItemRecommendService champItemRecommendService;
    @PostMapping
    public ResponseEntity<ApiResponse<ChampItemRecommendResponse>> create(
            @Valid @RequestBody ChampItemRecommendRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(champItemRecommendService.create(request))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChampItemRecommendResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ChampItemRecommendRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(champItemRecommendService.update(id, request))
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        champItemRecommendService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChampItemRecommendResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(champItemRecommendService.getById(id))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChampItemRecommendResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(champItemRecommendService.getAll())
        );
    }

    @GetMapping("/champion/{championId}")
    public ResponseEntity<ApiResponse<List<ChampItemRecommendResponse>>> getAllByChampionId(
            @PathVariable Long championId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(champItemRecommendService.getAllByChampionId(championId))
        );
    }
}