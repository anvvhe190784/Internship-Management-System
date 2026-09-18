package com.example.internship.repository;

import com.example.internship.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Integer> {
    List<AssessmentResult> findByAssignment_AssignmentId(Integer assignmentId);
    List<AssessmentResult> findByAssignment_AssignmentIdAndRound_RoundId(Integer assignmentId, Integer roundId);
    Optional<AssessmentResult> findByAssignment_AssignmentIdAndRound_RoundIdAndCriterion_CriterionId(Integer assignmentId, Integer roundId, Integer criterionId);
    boolean existsByAssignment_AssignmentIdAndRound_RoundIdAndCriterion_CriterionId(Integer assignmentId, Integer roundId, Integer criterionId);
}
