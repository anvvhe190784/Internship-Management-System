package com.example.internship.repository;

import com.example.internship.entity.EvaluationCriteria;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Integer> {
  Optional<EvaluationCriteria> findByCriterionName(String criterionName);

  boolean existsByCriterionName(String criterionName);
}
