package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.core.api.ApiResponse;
import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;
import org.example.services.SetsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sets")
public class SetsController {

    private final SetsService setsService;

    @PostMapping
    public ResponseEntity<ApiResponse<SetsResponse>> create(@Valid @RequestBody SetsRequest request) {
        SetsResponse response = setsService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SetsResponse>> getSetById(@PathVariable Long id) {
        SetsResponse response = setsService.getSetById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SetsResponse>>> getAllSet() {
        List<SetsResponse> response = setsService.getAllSet();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SetsResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SetsRequest request
    ) {
        SetsResponse response = setsService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        setsService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}