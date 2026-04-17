package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.item.ItemRequest;
import org.example.dto.item.ItemResponse;
import org.example.entities.Sets;

import java.util.List;

public interface ItemService {
    ItemResponse create (ItemRequest request);
    ItemResponse getItemById (Long id);
    List<ItemResponse> getAllPublishedItem();
    ItemResponse getActiveItemById(Long id);
    List<ItemResponse> getAllItem ();
    ItemResponse update (Long id, ItemRequest request);
    void delete (Long id);
    void deleteMany(List<Long> ids);
    PageResponse<ItemResponse> getItemsForCms(int page,
                                              int size,
                                              String keyword,
                                              Long setId,
                                              Boolean setDeleted,
                                              Boolean itemDeleted,
                                              String tier,
                                              String sortBy,
                                              String sortDir);

    PageResponse<ItemResponse> getPublishedItems(int page,
                                                 int size,
                                                 String keyword,
                                                 Long setId,
                                                 String tier,
                                                 String sortBy,
                                                 String sortDir);
}
