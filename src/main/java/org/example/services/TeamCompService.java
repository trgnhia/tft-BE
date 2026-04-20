package org.example.services;

import org.example.dto.teamcomp.TeamCompFilter;
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
    void deleteMany(List<Long> ids);

    void restore(Long id);
    void restoreMany(List<Long> ids);
    // Luồng User App
    Page<TeamCompResponse> filterTeamComps(TeamCompFilter filter, Pageable pageable);

    // Luồng CMS
    Page<TeamCompResponse> filterTeamCompsCms(TeamCompFilter filter, Pageable pageable);

}