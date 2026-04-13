package org.example.controller.champ_item_recommend;

import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.champ_item_recommend.ChampItemRecommendResponse;
import org.example.services.ChampItemRecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/champ-item-recommends")
@RequiredArgsConstructor
public class ChampItemRecommendController {

    private final ChampItemRecommendService champItemRecommendService;

    @GetMapping("/champion/{championId}")
    public ResponseEntity<ApiResponse<List<ChampItemRecommendResponse>>> getPublishedByChampionId(
            @PathVariable Long championId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(champItemRecommendService.getPublishedByChampionId(championId))
        );
    }
}