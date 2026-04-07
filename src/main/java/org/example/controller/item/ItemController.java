package org.example.controller.item;

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getById(@PathVariable Long id) {
        ItemResponse res = itemService.getActiveItemById(id);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAll() {
        List<ItemResponse> res = itemService.getAllPublishedItem();
        return ResponseEntity.ok(ApiResponse.success(res));
    }

}