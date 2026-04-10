package org.example.controller.teamcomp;

import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.teamcomp.TeamCompResponse;
import org.example.services.TeamCompService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team-comp")
public class TeamCompController {
    private final TeamCompService teamCompService;
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TeamCompResponse>>> filter(
            @RequestParam(required = false) Long setId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> styles,
            @RequestParam(required = false) Long championId,
            @PageableDefault(page = 1, size = 10) Pageable pageable
    ) {
        Page<TeamCompResponse> response = teamCompService.filterTeamComps(setId, keyword, styles, championId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
