package org.example.services;

import org.example.dto.teamcomp.TeamCompRequest;
import org.example.dto.teamcomp.TeamCompResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeamCompService {
    TeamCompResponse create(TeamCompRequest request);
    TeamCompResponse update(Long id, TeamCompRequest request);
    void delete(Long id);
    TeamCompResponse getById(Long id);

    Page<TeamCompResponse> filterTeamComps(
            Long setId,             // Lọc theo mùa (Dropdown Set 17)
            String keyword,         // Lọc theo text (Search bar)
            List<String> styles,    // Lọc theo lối chơi (Checkbox sidebar bên trái)
            Long championId,        // Lọc theo tướng (Dropdown Champions)
            Pageable pageable       // Phân trang
    );
}