package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.dto.item.ItemRequest;
import org.example.dto.item.ItemResponse;
import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;
import org.example.mapper.ItemMapper;
import org.example.repositories.ItemRepository;
import org.example.services.ItemService;
import org.example.services.SetsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepo;
    private final ItemMapper itemMapper;
    @Override
    public ItemResponse create(ItemRequest request) {
        return null;
    }

    @Override
    public ItemResponse getItemById(Long id) {
        return null;
    }

    @Override
    public List<ItemResponse> getAllItem() {
        return List.of();
    }

    @Override
    public ItemResponse update(Long id, ItemRequest request) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}