package org.example.controller.teamcomp;

import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.teamcomp.TeamCompFilter;
import org.example.dto.teamcomp.TeamCompResponse;
import org.example.services.TeamCompService;
import org.springdoc.core.annotations.ParameterObject;
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
            @ParameterObject @ModelAttribute TeamCompFilter filter,
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(teamCompService.filterTeamComps(filter, pageable))
        );
    }
}
