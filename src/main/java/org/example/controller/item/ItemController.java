package org.example.controller.item;

import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.item.ItemResponse;
import org.example.services.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getById(@PathVariable Long id) {
        ItemResponse res = itemService.getActiveItemById(id);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/published")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAll() {
        List<ItemResponse> res = itemService.getAllPublishedItem();
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> getPublishedItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long setId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(itemService.getPublishedItems(page, size, keyword, setId))
        );
    }
}
