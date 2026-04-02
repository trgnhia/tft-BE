package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;
import org.example.services.SetsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sets")
public class SetsController {

    private final SetsService setsService;

    @PostMapping
    public ResponseEntity<SetsResponse> create(@Valid @RequestBody SetsRequest request) {
        SetsResponse response = setsService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}