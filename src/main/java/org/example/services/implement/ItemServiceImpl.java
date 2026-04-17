package org.example.services.implement;

import lombok.RequiredArgsConstructor;

import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.core.api.PageResponse;
import org.example.dto.item.ItemRequest;
import org.example.dto.item.ItemResponse;
import org.example.entities.Sets;
import org.example.entities.item.Item;
import org.example.mapper.ItemMapper;
import org.example.repositories.ChampItemRecommendRepository;
import org.example.repositories.ItemRepository;
import org.example.repositories.SetsRepository;
import org.example.repositories.spec.ItemSpecification;
import org.example.services.ItemService;
import org.example.util.MessageUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepo;
    private final ItemMapper itemMapper;
    private final SetsRepository setRepo;
    private final ChampItemRecommendRepository champItemRecommendRepo;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "updatedAt", "name", "tier");
    private static final Set<String> ALLOWED_SORT_DIRS = Set.of("asc", "desc", "high", "low");

    // ---------- PUBLIC SERVICES ----------

    @Override
    public List<ItemResponse> getAllPublishedItem() {
        return itemRepo.findAllPublishedWithSets().stream()
                .map(itemMapper::toItemResponse)
                .toList();
    }

    @Override
    public ItemResponse getActiveItemById(Long id) {
        Item item = itemRepo.findPublishedById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_ITEM),
                        MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                        String.valueOf(id)
                ));
        return itemMapper.toItemResponse(item);
    }

    @Override
    public PageResponse<ItemResponse> getPublishedItems(int page,
                                                        int size,
                                                        String keyword,
                                                        Long setId,
                                                        String tier,
                                                        String sortBy,
                                                        String sortDir) {

        String normalizedSortBy = normalizeSortBy(sortBy);
        String normalizedSortDir = normalizeSortDir(sortDir);

        Specification<Item> spec = Specification.allOf(
                ItemSpecification.publicVisible(),
                ItemSpecification.hasKeyword(keyword),
                ItemSpecification.hasSetId(setId),
                ItemSpecification.hasTier(tier)
        );


        if ("tier".equals(normalizedSortBy)) {
            return getPublishedItemsSortByTier(page, size, spec, normalizedSortDir);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(getDirection(normalizedSortDir), normalizedSortBy)
        );

        Page<Item> itemPage = itemRepo.findAll(spec, pageable);
        return PageResponse.from(itemPage.map(itemMapper::toItemResponse));
    }



    // ---------- CMS SERVICES ----------

    @Override
    public ItemResponse getItemById(Long id) {
        Item item = getById(id);
        return itemMapper.toItemResponse(item);
    }

    @Override
    public List<ItemResponse> getAllItem() {
        return itemRepo.findAllWithSets().stream()
                .map(itemMapper::toItemResponse)
                .toList();
    }


    @Override
    public PageResponse<ItemResponse> getItemsForCms(int page,
                                                     int size,
                                                     String keyword,
                                                     Long setId,
                                                     Boolean setDeleted,
                                                     Boolean itemDeleted,
                                                     String tier,
                                                     String sortBy,
                                                     String sortDir) {

        String normalizedSortBy = normalizeSortBy(sortBy);
        String normalizedSortDir = normalizeSortDir(sortDir);

        Specification<Item> spec = Specification
                .allOf(ItemSpecification.hasKeyword(keyword))
                .and(ItemSpecification.hasSetId(setId))
                .and(ItemSpecification.hasSetDeleted(setDeleted))
                .and(ItemSpecification.hasItemDeleted(itemDeleted))
                .and(ItemSpecification.hasTier(tier));

        if ("tier".equals(normalizedSortBy)) {
            return getItemsForCmsSortByTier(page, size, spec, normalizedSortDir);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(getDirection(normalizedSortDir), normalizedSortBy)
        );

        Page<Item> itemPage = itemRepo.findAll(spec, pageable);
        return PageResponse.from(itemPage.map(itemMapper::toItemResponse));
    }


    @Override
    @Transactional
    public ItemResponse create(ItemRequest request) {
        Item item = itemMapper.toEntity(request);
        String normalizedName = normalizeName(request.getName());
        Sets sets =  setRepo.findById(request.getSetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS),
                        MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                        String.valueOf(request.getSetId())
                ));
        validateDuplicateName(normalizedName);
        item.setSets(sets);

        Item savedItem = itemRepo.save(item);
        return itemMapper.toItemResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = getById(id);
        String normalizedName = normalizeName(request.getName());
        validateDuplicateNameForUpdate(normalizedName, id);

        Sets sets = setRepo.findById(request.getSetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS),
                        MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                        String.valueOf(request.getSetId())
                ));

        itemMapper.updateEntity(request, item);
        item.setName(normalizedName);
        item.setSets(sets);

        Item savedItem = itemRepo.save(item);
        return itemMapper.toItemResponse(savedItem);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        Item item = getById(id);
        if (item.isDeleted()) {
            throw new ConflictException(
                    ErrorCode.ALREADY_DELETED,
                    Constants.MessageKey.ENTITY_ITEM,
                    Constants.MessageKey.FIELD_ID,
                    String.valueOf(id)
            );
        }
        item.setDeleted(true);
        champItemRecommendRepo.softDeleteByItemId(id);
        itemRepo.save(item);
    }

    @Override
    @Transactional
    public void deleteMany(List<Long> ids) {

        List<Item> items = itemRepo.findAllByIdInAndDeletedFalse(ids);

        if (items.size() != ids.size()) {
            throw new DataException(
                    ErrorCode.INCOMPLETE_DATA,
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_ITEM)
            );
        }

        items.forEach(item -> item.setDeleted(true));

        itemRepo.saveAll(items);

        champItemRecommendRepo.softDeleteByItemIds(ids);
    }

    // ---------- PRIVATE HELPER ----------


    private Item getById(Long id) {
        return itemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_ITEM),
                        MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                        String.valueOf(id)
                ));
    }
    private String normalizeName(String name) {
        return name.trim();
    }

    private void validateDuplicateName(String name) {
        if (itemRepo.existsByName(name)) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_ITEM),
                    MessageUtils.getMessage(Constants.MessageKey.FIELD_ITEM_NAME),
                    name
            );
        }
    }

    private void validateDuplicateNameForUpdate(String name, Long id) {
        if (itemRepo.existsByNameAndIdNot(name, id)) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_ITEM),
                    MessageUtils.getMessage(Constants.MessageKey.FIELD_ITEM_NAME),
                    name
            );
        }
    }


    private PageResponse<ItemResponse> getItemsForCmsSortByTier(int page,
                                                                int size,
                                                                Specification<Item> spec,
                                                                String sortDir) {

        List<Item> allItems = itemRepo.findAll(spec);
        sortItemsByTier(allItems, sortDir);

        return toManualPageResponse(allItems, page, size);
    }

    private PageResponse<ItemResponse> getPublishedItemsSortByTier(int page,
                                                                   int size,
                                                                   Specification<Item> spec,
                                                                   String sortDir) {

        List<Item> allItems = itemRepo.findAll(spec);
        sortItemsByTier(allItems, sortDir);

        return toManualPageResponse(allItems, page, size);
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank() || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return "createdAt";
        }
        return sortBy;
    }

    private String normalizeSortDir(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return "desc";
        }
        String normalized = sortDir.trim().toLowerCase();
        return ALLOWED_SORT_DIRS.contains(normalized) ? normalized : "desc";
    }

    private Sort.Direction getDirection(String sortDir) {
        return "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private void sortItemsByTier(List<Item> items, String sortDir) {
        Comparator<Item> comparator = Comparator
                .comparingInt((Item item) -> tierOrder(item.getTier()))
                .thenComparing(Item::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        items.sort(comparator);
    }

    private int tierOrder(String tier) {
        if (tier == null) {
            return 999;
        }

        return switch (tier.toUpperCase()) {
            case "S" -> 1;
            case "A" -> 2;
            case "B" -> 3;
            case "C" -> 4;
            default -> 999;
        };
    }

    private PageResponse<ItemResponse> toManualPageResponse(List<Item> items, int page, int size) {
        int start = page * size;
        int end = Math.min(start + size, items.size());

        List<ItemResponse> content = (start >= items.size())
                ? Collections.emptyList()
                : items.subList(start, end).stream()
                .map(itemMapper::toItemResponse)
                .toList();

        Page<ItemResponse> responsePage = new PageImpl<>(
                content,
                PageRequest.of(page, size),
                items.size()
        );

        return PageResponse.from(responsePage);
    }
}