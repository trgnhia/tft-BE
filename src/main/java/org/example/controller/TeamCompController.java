package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.teamcomp.*;
import org.example.services.TeamCompService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team-comp")
public class TeamCompController {
    private final TeamCompService teamCompService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeamCompResponse>> create(@Valid @RequestBody TeamCompRequest request) {
        TeamCompResponse response = teamCompService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamCompResponse>> getById(@PathVariable Long id) {
        TeamCompResponse response = teamCompService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
