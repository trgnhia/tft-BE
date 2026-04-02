package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ResourceNotFoundException;
import org.example.dto.teamcomp.TeamCompRequest;
import org.example.dto.teamcomp.TeamCompResponse;
import org.example.entities.Champ;
import org.example.entities.TeamComp;
import org.example.entities.TeamCompChamp;
import org.example.mapper.TeamCompMapper;
import org.example.repositories.ChampRepository;
import org.example.repositories.TeamCompRepository;
import org.example.services.TeamCompService;
import org.example.util.MessageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamCompServiceImpl implements TeamCompService {
    private final TeamCompRepository teamCompRepository;
    private final ChampRepository champRepository;
    private final TeamCompMapper teamCompMapper;
    @Override
    @Transactional
    public TeamCompResponse create(TeamCompRequest request) {
        TeamComp teamComp = teamCompMapper.toEntity(request);
        final TeamComp savedTeamComp = teamCompRepository.save(teamComp);

        if (request.getChampionIds() != null && !request.getChampionIds().isEmpty()) {
            List<Champ> foundChamps = champRepository.findAllById(request.getChampionIds());
            List<TeamCompChamp> joinList = foundChamps.stream().map(champ -> {
                TeamCompChamp tcc = new TeamCompChamp();
                tcc.setTeamCompId(savedTeamComp.getId());
                tcc.setChampionId(champ.getId());
                tcc.setTeamComp(savedTeamComp);
                tcc.setChamp(champ);
                return tcc;
            }).collect(Collectors.toList());
            savedTeamComp.setTeamCompChamps(joinList);
            teamCompRepository.save(savedTeamComp);
        }
        return teamCompMapper.toResponse(savedTeamComp);
    }

    @Override
    public TeamCompResponse getById(Long id) {
        TeamComp teamComp = teamCompRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.TEAM_NOT_FOUND,id)
                ));
        return teamCompMapper.toResponse(teamComp);
    }

    @Override
    public TeamCompResponse update(Long id, TeamCompRequest request) {
        return null;
    }


    @Override
    public void delete(Long id) {

    }

    @Override
    public Page<TeamCompResponse> filterTeamComps(Long setId, String keyword, List<String> styles, Long championId, Pageable pageable) {
        return null;
    }
}
