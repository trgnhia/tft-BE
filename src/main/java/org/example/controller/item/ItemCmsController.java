package org.example.controller.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.champs.BulkDeleteRequest;
import org.example.dto.item.ItemRequest;
import org.example.dto.item.ItemResponse;
import org.example.services.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/items")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
public class ItemCmsController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long setId,
            @RequestParam(required = false) Boolean setDeleted,
            @RequestParam(required = false) Boolean itemDeleted,
            @RequestParam(required = false) String tier,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        itemService.getItemsForCms(
                                page,
                                size,
                                keyword,
                                setId,
                                setDeleted,
                                itemDeleted,
                                tier,
                                sortBy,
                                sortDir
                        )
                )
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


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteMany(@Valid @RequestBody BulkDeleteRequest request) {
        itemService.deleteMany(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}