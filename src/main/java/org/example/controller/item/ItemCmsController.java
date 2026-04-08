package org.example.controller.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.item.ItemRequest;
import org.example.dto.item.ItemResponse;
import org.example.services.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cms/items")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
public class ItemCmsController {

    private final ItemService itemService;

//    @GetMapping
//    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAll() {
//        return ResponseEntity.ok(
//                ApiResponse.success(itemService.getAllItem())
//        );
//    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long setId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(itemService.getItems(page, size, keyword, setId))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create(
            @Valid @RequestBody ItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(itemService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(itemService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}