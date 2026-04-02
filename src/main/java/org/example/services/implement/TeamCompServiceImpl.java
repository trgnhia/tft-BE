package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.dto.teamcomp.TeamCompRequest;
import org.example.dto.teamcomp.TeamCompResponse;
import org.example.services.TeamCompService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamCompServiceImpl implements TeamCompService {

    @Override
    public TeamCompResponse create(TeamCompRequest request) {
        return null;
    }

    @Override
    public TeamCompResponse update(Long id, TeamCompRequest request) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public TeamCompResponse getById(Long id) {
        return null;
    }

    @Override
    public Page<TeamCompResponse> filterTeamComps(Long setId, String keyword, List<String> styles, Long championId, Pageable pageable) {
        return null;
    }
}
