package org.example.services.implement;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.Constants;
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
import org.example.util.MessageUtils;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class ChampItemRecommendServiceImpl implements ChampItemRecommendService {

    private final ChampItemRecommendRepository repo;
    private final ChampItemRecommendMapper mapper;
    private final ItemRepository itemRepo;

    // ---------- PUBLIC SERVICES ----------

    @Override
    public List<ChampItemRecommendResponse> getPublishedByChampionId(Long championId) {
        return repo.findAllPublishedByChampionId(championId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ---------- CMS SERVICES ----------

    @Override
    public ChampItemRecommendResponse create(ChampItemRecommendRequest request) {

        if (repo.existsByChampionIdAndItemIdAndDeletedFalse(
                request.getChampionId(),
                request.getItemId()
        )) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_CHAMP_ITEM_RECOMMEND)
            );
        }

        Item item = itemRepo.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_ITEM),
                        MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                        String.valueOf(request.getItemId())
                ));

        ChampItemRecommend entity = mapper.toEntity(request);
        entity.setItem(item);
        entity.setDeleted(false);

        ChampItemRecommend saved = repo.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public ChampItemRecommendResponse update(Long id, ChampItemRecommendRequest request) {

        ChampItemRecommend entity = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageUtils.getMessage(Constants.MessageKey.ENTITY_CHAMP_ITEM_RECOMMEND)
                        )
                );

        if (repo.existsByChampionIdAndItemIdAndIdNotAndDeletedFalse(
                request.getChampionId(),
                request.getItemId(),
                id
        )) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_CHAMP_ITEM_RECOMMEND)
            );
        }

        Item item = itemRepo.findByIdAndDeletedFalse(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_ITEM),
                        MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                        String.valueOf(request.getItemId())
                ));

        mapper.updateEntity(request, entity);
        entity.setItem(item);

        ChampItemRecommend saved = repo.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        ChampItemRecommend entity = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_CHAMP_ITEM_RECOMMEND)
                ));

        entity.setDeleted(true);
        repo.save(entity);
    }

    @Override
    public ChampItemRecommendResponse getById(Long id) {
        ChampItemRecommend entity = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_CHAMP_ITEM_RECOMMEND)
                ));

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
    public List<ChampItemRecommendResponse> getAllByChampionId(Long championId) {
        return repo.findAllByChampionIdForCms(championId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}