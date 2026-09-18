package com.example.internship.repository;

import com.example.internship.entity.RoundCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoundCriteriaRepository extends JpaRepository<RoundCriteria, Integer> {
    List<RoundCriteria> findByRound_RoundId(Integer roundId);
    Optional<RoundCriteria> findByRound_RoundIdAndCriterion_CriterionId(Integer roundId, Integer criterionId);
    boolean existsByRound_RoundIdAndCriterion_CriterionId(Integer roundId, Integer criterionId);
}
