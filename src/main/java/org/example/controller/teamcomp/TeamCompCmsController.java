package org.example.controller.teamcomp;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.teamcomp.*;
import org.example.services.TeamCompService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/team-comp")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'USER')")
public class TeamCompCmsController {
    private final TeamCompService teamCompService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<TeamCompResponse>> create(@Valid @RequestBody TeamCompRequest request) {
        TeamCompResponse response = teamCompService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamCompResponse>> getById(@PathVariable Long id) {
        TeamCompResponse response = teamCompService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<TeamCompResponse>> update(@PathVariable Long id, @Valid @RequestBody TeamCompRequest request) {
        TeamCompResponse response = teamCompService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<TeamCompResponse>> patchUpdate(
            @PathVariable Long id,
            @RequestBody TeamCompRequest request) {
        TeamCompResponse response = teamCompService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teamCompService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
