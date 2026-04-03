package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.item.ItemRequest;
import org.example.dto.item.ItemResponse;
import org.example.services.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        return ResponseEntity.ok(
                ApiResponse.success(
                        res
                )
        );
    }

}
