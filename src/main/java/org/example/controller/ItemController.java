package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.item.ItemRequest;
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

    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create(
            @Valid @RequestBody ItemRequest request
    ) {
        ItemResponse res = itemService.create(request);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getById(@PathVariable Long id) {
        ItemResponse res = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAll() {
        List<ItemResponse> res = itemService.getAllItem();
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request
    ) {
        ItemResponse res = itemService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}