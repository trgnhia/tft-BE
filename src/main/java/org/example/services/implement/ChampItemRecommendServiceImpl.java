package org.example.services.implement;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.dto.champ_item_recommend.ChampItemRecommendRequest;
import org.example.dto.champ_item_recommend.ChampItemRecommendResponse;
import org.example.entities.ChampItemRecommend;
import org.example.entities.item.Item;
import org.example.mapper.ChampItemRecommendMapper;
import org.example.repositories.ChampItemRecommendRepository;
import org.example.repositories.ItemRepository;
import org.example.services.ChampItemRecommendService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class ChampItemRecommendServiceImpl implements ChampItemRecommendService {

    private final ChampItemRecommendRepository repo;
    private final ChampItemRecommendMapper mapper;
    private final ItemRepository itemRepo;

    @Override
    public ChampItemRecommendResponse create(ChampItemRecommendRequest request) {

        if (repo.existsByChampionIdAndItemIdAndDeletedFalse(
                request.getChampionId(),
                request.getItemId()
        )) {
            throw new ConflictException("Champ item recommend already exists");
        }

        Item item = itemRepo.findByIdAndDeletedFalse(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        ChampItemRecommend entity = mapper.toEntity(request);
        entity.setItem(item);
        entity.setDeleted(false);

        ChampItemRecommend saved = repo.save(entity);
        return mapper.toResponse(saved);
    }

    // ================= UPDATE =================
    @Override
    public ChampItemRecommendResponse update(Long id, ChampItemRecommendRequest request) {

        ChampItemRecommend entity = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Champ item recommend not found"));

        if (repo.existsByChampionIdAndItemIdAndIdNotAndDeletedFalse(
                request.getChampionId(),
                request.getItemId(),
                id
        )) {
            throw new ConflictException("Champ item recommend already exists");
        }

        Item item = itemRepo.findByIdAndDeletedFalse(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        mapper.updateEntity(request, entity);
        entity.setItem(item);

        ChampItemRecommend saved = repo.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        ChampItemRecommend entity = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Champ item recommend not found"));

        entity.setDeleted(true);
        repo.save(entity);
    }
    @Override
    public ChampItemRecommendResponse getById(Long id) {
        ChampItemRecommend entity = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Champ item recommend not found"));

        return mapper.toResponse(entity);
    }

    @Override
    public List<ChampItemRecommendResponse> getAll() {
        return repo.findAllActive()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<ChampItemRecommendResponse> getByChampionId(Long championId) {
        return repo.findAllByChampionId(championId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}