package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.constant.Constants;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.dto.teamcomp.TeamCompFilter;
import org.example.dto.teamcomp.TeamCompRequest;
import org.example.dto.teamcomp.TeamCompResponse;
import org.example.entities.champ.Champ;
import org.example.entities.TeamComp;
import org.example.entities.TeamCompChamp;
import org.example.mapper.TeamCompMapper;
import org.example.repositories.ChampRepository;
import org.example.repositories.teamcomp.TeamCompRepository;
import org.example.services.TeamCompService;
import org.example.util.MessageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        if (teamCompRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_TEAMS),
                    request.getName()
            );
        }
        TeamComp teamComp = teamCompMapper.toEntity(request);
        if (teamComp.getTeamCompChamps() != null) {
            teamComp.getTeamCompChamps().clear();
        }
        TeamComp savedTeamComp = teamCompRepository.save(teamComp);

        final Long teamCompId = savedTeamComp.getId();

        if (request.getChampionIds() != null && !request.getChampionIds().isEmpty()) {

            Set<Long> uniqueChampIds = new HashSet<>(request.getChampionIds());
            List<Champ> foundChamps = champRepository.findAllById(uniqueChampIds);

            if (foundChamps.size() != uniqueChampIds.size()) {
                throw new ResourceNotFoundException(MessageUtils.getMessage(Constants.MessageKey.ERROR_NOT_FOUND));
            }

            List<TeamCompChamp> joinList = foundChamps.stream().map(champ -> {
                TeamCompChamp tcc = new TeamCompChamp();

                tcc.setTeamCompId(teamCompId);
                tcc.setChampionId(champ.getId());

                tcc.setTeamComp(savedTeamComp);
                tcc.setChamp(champ);
                return tcc;
            }).toList();

            savedTeamComp.setTeamCompChamps(joinList);
            TeamComp finalTeamComp = teamCompRepository.save(savedTeamComp);
            return teamCompMapper.toResponse(finalTeamComp);
        }
        return teamCompMapper.toResponse(savedTeamComp);
    }

    @Override
    public TeamCompResponse getById(Long id) {
        TeamComp teamComp = teamCompRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_TEAMS),
                        "id " ,String.valueOf(id)));
        return teamCompMapper.toResponse(teamComp);
    }

    @Override
    @Transactional
    public TeamCompResponse update(Long id, TeamCompRequest request) {
        TeamComp teamComp = teamCompRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.getMessage(Constants.MessageKey.ENTITY_TEAMS),
                        "id " ,String.valueOf(id)));
        if (!teamComp.getName().equals(request.getName()) &&
                teamCompRepository.existsByNameAndIdNotAndDeletedFalse(request.getName(), id)) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_TEAMS),
                    request.getName()
            );
        }

        teamCompMapper.updateEntity(request, teamComp);


        if (request.getChampionIds() != null) {
            Set<Long> uniqueChampIds = new HashSet<>(request.getChampionIds());
            List<Champ> champs = champRepository.findAllById(uniqueChampIds);

            if (champs.size() != uniqueChampIds.size()) {
                throw new ResourceNotFoundException(MessageUtils.getMessage(Constants.MessageKey.ERROR_NOT_FOUND));
            }

            teamComp.getTeamCompChamps().removeIf(tcc -> !uniqueChampIds.contains(tcc.getChampionId()));

            Set<Long> existingChampIds = teamComp.getTeamCompChamps().stream()
                    .map(TeamCompChamp::getChampionId)
                    .collect(Collectors.toSet());

            Map<Long, Champ> champMap = champs.stream()
                    .collect(Collectors.toMap(Champ::getId, c -> c));

            for (Long champId : uniqueChampIds) {
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
    public void restore(Long id) {
        TeamComp teamComp = teamCompRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_TEAMS),
                        "id ", String.valueOf(id)));

        if (teamComp.getSets()!= null && Boolean.TRUE.equals(teamComp.getSets().isDeleted())) {
            throw new ConflictException("Không thể khôi phục vì mùa đã bị vô hiệu hóa");
        }

        if (teamComp.isDeleted()) {
            teamComp.setDeleted(false);
            teamCompRepository.save(teamComp);
            log.info("Restored teamComp id={}", id);
        }
    }

    @Override
    @Transactional
    public void restoreMany(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        List<TeamComp> teamComps = teamCompRepository.findAllByIdIn(ids);

        if (teamComps.size() != ids.size()) {
            throw new ResourceNotFoundException(
                    MessageUtils.getMessage(Constants.MessageKey.ERROR_NOT_FOUND)
            );
        }

        for (TeamComp tc : teamComps) {
            if (tc.getSets() != null && Boolean.TRUE.equals(tc.getSets().isDeleted())) {
                throw new ConflictException("Một số đội hình không thể khôi phục vì mùa đã bị vô hiệu hóa");
            }
        }

        teamComps.forEach(tc -> {
            if (tc.isDeleted()) {
                tc.setDeleted(false);
                log.info("Restored teamComp id={}", tc.getId());
            }
        });

        teamCompRepository.saveAll(teamComps);
    }


    @Override
    @Transactional
    public void delete(Long id) {
        TeamComp teamComp = teamCompRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.getMessage(Constants.MessageKey.ENTITY_TEAMS),
                        "id " ,String.valueOf(id)));

        teamComp.setDeleted(true);
        teamCompRepository.save(teamComp);
    }
    @Override
    @Transactional
    public void deleteMany(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<TeamComp> teamComps = teamCompRepository.findAllByIdIn(ids);
        if (teamComps.size() != ids.size()) {
            throw new ResourceNotFoundException(
                    MessageUtils.getMessage(Constants.MessageKey.ERROR_NOT_FOUND)
            );
        }
        teamComps.forEach(teamComp -> teamComp.setDeleted(true));

        teamCompRepository.saveAll(teamComps);
    }
    @Override
    public Page<TeamCompResponse> filterTeamComps(TeamCompFilter filter, Pageable pageable) {
        Page<Long> pagedIds = teamCompRepository.filterTeamCompIds(filter, pageable);
        return mapToPageResponsePreserveOrder(pagedIds, pageable);
    }

    @Override
    public Page<TeamCompResponse> filterTeamCompsCms(TeamCompFilter filter, Pageable pageable) {
        Page<Long> pagedIds = teamCompRepository.filterTeamCompIdsCms(filter, pageable);

        return mapToPageResponsePreserveOrder(pagedIds, pageable);
    }

    private Page<TeamCompResponse> mapToPageResponsePreserveOrder(Page<Long> pagedIds, Pageable pageable) {
        if (pagedIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> orderedIds = pagedIds.getContent();

        List<TeamComp> teamComps = teamCompRepository.findAllByIdIn(orderedIds);

        Map<Long, TeamComp> map = teamComps.stream()
                .collect(Collectors.toMap(TeamComp::getId, t -> t));

        List<TeamCompResponse> responses = orderedIds.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(teamCompMapper::toResponse)
                .toList();

        return new PageImpl<>(responses, pageable, pagedIds.getTotalElements());
    }
}