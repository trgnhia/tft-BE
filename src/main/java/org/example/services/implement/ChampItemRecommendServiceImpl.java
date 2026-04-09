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
        return toResponseList(repo.findAllPublishedByChampionId(championId));
    }

    // ---------- CMS SERVICES ----------

    @Override
    public ChampItemRecommendResponse create(ChampItemRecommendRequest request) {
        validateDuplicate(request.getChampionId(), request.getItemId(), null);

        Item item = getPublishedItemById(request.getItemId());

        ChampItemRecommend entity = mapper.toEntity(request);
        entity.setItem(item);
        entity.setDeleted(false);

        return mapper.toResponse(repo.save(entity));
    }

    @Override
    public ChampItemRecommendResponse update(Long id, ChampItemRecommendRequest request) {
        ChampItemRecommend entity = getActiveRecommendById(id);
        validateDuplicate(request.getChampionId(), request.getItemId(), id);

        Item item = getPublishedItemById(request.getItemId());

        mapper.updateEntity(request, entity);
        entity.setItem(item);

        return mapper.toResponse(repo.save(entity));
    }

    @Override
    public void delete(Long id) {
        ChampItemRecommend entity = getActiveRecommendById(id);
        entity.setDeleted(true);
        repo.save(entity);
    }

    @Override
    public ChampItemRecommendResponse getById(Long id) {
        return mapper.toResponse(getActiveRecommendById(id));
    }

    @Override
    public List<ChampItemRecommendResponse> getAll() {
        return toResponseList(repo.findAllActive());
    }

    @Override
    public List<ChampItemRecommendResponse> getAllByChampionId(Long championId) {
        return toResponseList(repo.findAllByChampionIdForCms(championId));
    }


    private ChampItemRecommend getActiveRecommendById(Long id) {
        return repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_CHAMP_ITEM_RECOMMEND)
                ));
    }

    private Item getPublishedItemById(Long itemId) {
        return itemRepo.findPublishedById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_ITEM),
                        MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                        String.valueOf(itemId)
                ));
    }

    private void validateDuplicate(Long championId, Long itemId, Long excludeId) {
        boolean exists = (excludeId == null)
                ? repo.existsByChampionIdAndItemIdAndDeletedFalse(championId, itemId)
                : repo.existsByChampionIdAndItemIdAndIdNotAndDeletedFalse(championId, itemId, excludeId);

        if (exists) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_CHAMP_ITEM_RECOMMEND)
            );
        }
    }

    private List<ChampItemRecommendResponse> toResponseList(List<ChampItemRecommend> entities) {
        return entities.stream()
                .map(mapper::toResponse)
                .toList();
    }
}