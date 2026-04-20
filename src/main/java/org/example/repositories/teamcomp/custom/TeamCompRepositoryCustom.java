package org.example.repositories.teamcomp.custom;


import org.example.dto.teamcomp.TeamCompFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TeamCompRepositoryCustom {
    Page<Long> filterTeamCompIds(TeamCompFilter filter, Pageable pageable);
    Page<Long> filterTeamCompIdsCms(TeamCompFilter filter, Pageable pageable);
}