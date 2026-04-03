package org.example.services.implement;

import lombok.RequiredArgsConstructor;

import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.dto.item.ItemRequest;
import org.example.dto.item.ItemResponse;
import org.example.entities.Sets;
import org.example.entities.item.Item;
import org.example.mapper.ItemMapper;
import org.example.repositories.ItemRepository;
import org.example.repositories.SetsRepository;
import org.example.services.ItemService;
import org.example.util.MessageUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepo;
    private final ItemMapper itemMapper;
    private final SetsRepository setRepo;
    @Override
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
    public ItemResponse getItemById(Long id) {
        Item item = getById(id);
        return itemMapper.toItemResponse(item);
    }

    @Override
    public List<ItemResponse> getAllItem() {
        return itemRepo.findAll().stream()
                .map(itemMapper::toItemResponse)
                .toList();
    }

    @Override
    public ItemResponse update(Long id, ItemRequest request) {
        return null;
    }

    @Override
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

    private Item getById (Long id) {
        return itemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MessageKey.ENTITY_ITEM,
                        Constants.MessageKey.FIELD_ID,
                        String.valueOf(id)));
    }
    private String normalizeName(String name) {
        return name.trim();
    }

    private void validateDuplicateName(String name) {
        if (itemRepo.existsByName(name)) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS),
                    MessageUtils.getMessage(Constants.MessageKey.FIELD_SETS_NAME),
                    name
            );
        }
    }
}