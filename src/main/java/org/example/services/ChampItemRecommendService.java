package org.example.services;

import org.example.dto.champ_item_recommend.ChampItemRecommendRequest;
import org.example.dto.champ_item_recommend.ChampItemRecommendResponse;

import java.util.List;

public interface ChampItemRecommendService {
    ChampItemRecommendResponse create(ChampItemRecommendRequest request);

    ChampItemRecommendResponse update(Long id, ChampItemRecommendRequest request);

    void delete(Long id);

    ChampItemRecommendResponse getById(Long id);

    List<ChampItemRecommendResponse> getAll();

    List<ChampItemRecommendResponse> getPublishedByChampionId(Long championId);
    List<ChampItemRecommendResponse> getAllByChampionId(Long championId);
}
