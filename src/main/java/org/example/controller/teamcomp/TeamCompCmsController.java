package org.example.controller.teamcomp;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.teamcomp.*;
import org.example.services.TeamCompService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    @GetMapping
    public ResponseEntity<ApiResponse<Page<TeamCompResponse>>> filterCms(
            @ParameterObject @ModelAttribute TeamCompFilter filter,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(teamCompService.filterTeamCompsCms(filter, pageable))
        );
    }

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

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        teamCompService.restore(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ApiResponse<Void> restoreMany(@RequestBody List<Long> ids) {
        teamCompService.restoreMany(ids);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teamCompService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Void>> deleteMany(@RequestBody List<Long> ids) {
        teamCompService.deleteMany(ids);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
