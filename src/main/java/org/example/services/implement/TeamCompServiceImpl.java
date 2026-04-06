package org.example.services.implement;

import jakarta.validation.Valid;
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
import org.example.util.FilterUtil;
import org.example.util.MessageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamCompServiceImpl implements TeamCompService {
    private final TeamCompRepository teamCompRepository;
    private final ChampRepository champRepository;
    private final TeamCompMapper teamCompMapper;
    private final FilterUtil filterUtil;
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
            if (savedTeamComp.getTeamCompChamps() == null) {
                savedTeamComp.setTeamCompChamps(new ArrayList<>());
            }
            savedTeamComp.getTeamCompChamps().addAll(joinList);

            teamCompRepository.save(savedTeamComp);
        }
        return teamCompMapper.toResponse(savedTeamComp);
    }

    @Override
    public TeamCompResponse getById(Long id) {
        filterUtil.enableDeletedFilter();
        TeamComp teamComp = teamCompRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_TEAMS),
                        "id " + String.valueOf(id)));
        return teamCompMapper.toResponse(teamComp);
    }

    @Transactional
    public TeamCompResponse update(Long id, TeamCompRequest request) {
        filterUtil.enableDeletedFilter();
        TeamComp teamComp = teamCompRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.getMessage(Constants.MessageKey.ENTITY_TEAMS),
                        "id " + String.valueOf(id)));
        teamCompMapper.updateEntity(request, teamComp);
        if (request.getChampionIds() != null) {

            Set<Long> requestChampIds = request.getChampionIds().stream().collect(Collectors.toSet());

            teamComp.getTeamCompChamps().removeIf(tcc -> !requestChampIds.contains(tcc.getChampionId()));

            Set<Long> existingChampIds = teamComp.getTeamCompChamps().stream()
                    .map(TeamCompChamp::getChampionId)
                    .collect(Collectors.toSet());

            List<Champ> champs = champRepository.findAllById(request.getChampionIds());
            Map<Long, Champ> champMap = champs.stream()
                    .collect(Collectors.toMap(Champ::getId, c -> c));

            for (Long champId : request.getChampionIds()) {
                if (!existingChampIds.contains(champId)) {
                    Champ champ = champMap.get(champId);
                    if (champ != null) {
                        TeamCompChamp tcc = new TeamCompChamp();
                        tcc.setTeamCompId(teamComp.getId());
                        tcc.setChampionId(champ.getId());
                        tcc.setTeamComp(teamComp);
                        tcc.setChamp(champ);

                        teamComp.getTeamCompChamps().add(tcc);
                    }
                }
            }
        }
        TeamComp updatedTeamComp = teamCompRepository.save(teamComp);

        return teamCompMapper.toResponse(updatedTeamComp);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TeamComp teamComp = teamCompRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.getMessage(Constants.MessageKey.ENTITY_TEAMS),
                        "id " + String.valueOf(id)));

        teamComp.setDeleted(true);

        teamCompRepository.save(teamComp);
    }

    @Override
    public Page<TeamCompResponse> filterTeamComps(Long setId, String keyword, List<String> styles, Long championId, Pageable pageable) {
        return null;
    }
}
