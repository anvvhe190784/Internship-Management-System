package com.example.internship.repository;

import com.example.internship.entity.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Integer> {
    Optional<EvaluationCriteria> findByCriterionName(String criterionName);
    boolean existsByCriterionName(String criterionName);
}
