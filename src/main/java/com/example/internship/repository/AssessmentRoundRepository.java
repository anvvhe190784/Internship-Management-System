package com.example.internship.repository;

import com.example.internship.entity.AssessmentRound;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentRoundRepository extends JpaRepository<AssessmentRound, Integer> {

    @Override
    @EntityGraph(attributePaths = { "phase" })
    List<AssessmentRound> findAll();

    @Override
    @EntityGraph(attributePaths = { "phase" })
    Optional<AssessmentRound> findById(Integer id);

    @EntityGraph(attributePaths = { "phase" })
    List<AssessmentRound> findByPhase_PhaseId(Integer phaseId);
}
