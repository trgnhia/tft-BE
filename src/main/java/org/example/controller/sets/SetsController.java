package org.example.controller.sets;

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
    @GetMapping
    public ResponseEntity<ApiResponse<List<SetsResponse>>> getAllPublishedSet() {
        List<SetsResponse> response = setsService.getAllPublishedSet();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}