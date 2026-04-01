package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.set.SetRequest;
import org.example.dto.set.SetResponse;
import org.example.services.SetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sets")
public class SetController {

    private final SetService setService;

    @PostMapping
    public ResponseEntity<SetResponse> create(@Valid @RequestBody SetRequest request) {
        SetResponse response = setService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}