package org.example.repository;

import org.example.entities.ChampTrait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChampTraitRepository extends JpaRepository<ChampTrait, Long> {

    List<ChampTrait> findByChamp_Id(Long champId);
    List<ChampTrait> findByTrait_Id(Long traitId);
    boolean existsByChamp_IdAndTrait_Id(Long champId, Long traitId);
    void deleteByChamp_IdAndTrait_Id(Long champId, Long traitId);
    void deleteByChamp_Id(Long champId);
}