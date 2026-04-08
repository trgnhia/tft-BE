package org.example.services.implement;

import lombok.RequiredArgsConstructor;

import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.core.api.PageResponse;
import org.example.dto.item.ItemRequest;
import org.example.dto.item.ItemResponse;
import org.example.entities.Sets;
import org.example.entities.item.Item;
import org.example.mapper.ItemMapper;
import org.example.repositories.ItemRepository;
import org.example.repositories.SetsRepository;
import org.example.services.ItemService;
import org.example.util.MessageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepo;
    private final ItemMapper itemMapper;
    private final SetsRepository setRepo;


    @Override
    public List<ItemResponse> getAllPublishedItem() {
        return itemRepo.findAllActiveWithSets().stream()
                .map(itemMapper::toItemResponse)
                .toList();
    }

    @Override
    public ItemResponse getActiveItemById(Long id) {
        Item item = itemRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_ITEM),
                        MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                        String.valueOf(id)
                ));
        return itemMapper.toItemResponse(item);
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
    public PageResponse<ItemResponse> getItems(int page, int size, String keyword, Long setId) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        String normalizedKeyword = (keyword == null || keyword.trim().isEmpty())
                ? null
                : keyword.trim();

        Page<Item> itemPage = itemRepo.searchItems(normalizedKeyword, setId, pageable);
        Page<ItemResponse> responsePage = itemPage.map(itemMapper::toItemResponse);
        return PageResponse.from(responsePage);
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
        itemRepo.save(item);
    }

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
}