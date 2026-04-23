package org.example.repositories.teamcomp;

import org.example.entities.TeamComp;
import org.example.repositories.teamcomp.custom.TeamCompRepositoryCustom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamCompRepository extends JpaRepository<TeamComp, Long>, TeamCompRepositoryCustom {
    Optional<TeamComp> findByIdAndDeletedFalse(Long id);
    Optional<TeamComp> findById(Long id);
    boolean existsByNameAndDeletedFalse(String name);
    boolean existsByNameAndIdNotAndDeletedFalse(String name, Long id);
    @EntityGraph(attributePaths = {"sets"})
    List<TeamComp> findAllByIdIn(List<Long> ids);
}
