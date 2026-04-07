package org.example.controller.sets;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;
import org.example.services.SetsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/sets")
@PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
public class SetsCmsController {

    private final SetsService setsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SetsResponse>>> getAllSetCms() {
        return ResponseEntity.ok(
                ApiResponse.success(setsService.getAllSet())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SetsResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(setsService.getSetById(id))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SetsResponse>> create(@Valid @RequestBody SetsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(setsService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SetsResponse>> update(
            @PathVariable Long id,
            @RequestBody SetsRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(setsService.update(id, request))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        setsService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}