package org.example.repositories;

import org.example.entities.TeamComp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamCompRepository extends JpaRepository<TeamComp, Long> {
    Optional<TeamComp> findByIdAndDeletedFalse(Long id);
}
