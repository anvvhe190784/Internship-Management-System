package com.example.internship.repository;

import com.example.internship.entity.AssessmentRound;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRoundRepository extends JpaRepository<AssessmentRound, Integer> {

  @Override
  @EntityGraph(attributePaths = {"phase"})
  List<AssessmentRound> findAll();

  @Override
  @EntityGraph(attributePaths = {"phase"})
  Optional<AssessmentRound> findById(Integer id);

  @EntityGraph(attributePaths = {"phase"})
  List<AssessmentRound> findByPhase_PhaseId(Integer phaseId);
}
