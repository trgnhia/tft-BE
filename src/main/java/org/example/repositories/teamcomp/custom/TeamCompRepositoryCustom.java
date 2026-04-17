package org.example.repositories.teamcomp.custom;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TeamCompRepositoryCustom {
    Page<Long> filterTeamCompIds(Long setId, String keyword, List<String> styles, Long championId, Pageable pageable);

    Page<Long> filterTeamCompIdsCms(
            Long setId,
            String keyword,
            List<String> styles,
            Long championId,
            Boolean deleted,
            Boolean setDeleted,
            Pageable pageable
    );
}