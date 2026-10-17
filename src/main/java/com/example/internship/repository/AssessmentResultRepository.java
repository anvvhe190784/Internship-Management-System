package com.example.internship.repository;

import com.example.internship.entity.AssessmentResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Integer> {

  @Override
  @EntityGraph(
      attributePaths = {
        "assignment.student.user",
        "assignment.mentor.user",
        "round",
        "criterion",
        "evaluatedBy"
      })
  List<AssessmentResult> findAll();

  @Override
  @EntityGraph(
      attributePaths = {
        "assignment.student.user",
        "assignment.mentor.user",
        "round",
        "criterion",
        "evaluatedBy"
      })
  Optional<AssessmentResult> findById(Integer id);

  @EntityGraph(
      attributePaths = {
        "assignment.student.user",
        "assignment.mentor.user",
        "round",
        "criterion",
        "evaluatedBy"
      })
  List<AssessmentResult> findByAssignment_AssignmentId(Integer assignmentId);

  @EntityGraph(
      attributePaths = {
        "assignment.student.user",
        "assignment.mentor.user",
        "round",
        "criterion",
        "evaluatedBy"
      })
  List<AssessmentResult> findByAssignment_AssignmentIdAndRound_RoundId(
      Integer assignmentId, Integer roundId);

  @Query(
      "SELECT r FROM AssessmentResult r "
          + "WHERE (:assignmentId IS NULL OR r.assignment.assignmentId = :assignmentId) "
          + "AND (:roundId IS NULL OR r.round.roundId = :roundId) "
          + "AND (:studentId IS NULL OR r.assignment.student.studentId = :studentId) "
          + "AND (:mentorId IS NULL OR r.assignment.mentor.mentorId = :mentorId)")
  @EntityGraph(
      attributePaths = {
        "assignment.student.user",
        "assignment.mentor.user",
        "round",
        "criterion",
        "evaluatedBy"
      })
  List<AssessmentResult> findWithFilters(
      @Param("assignmentId") Integer assignmentId,
      @Param("roundId") Integer roundId,
      @Param("studentId") Integer studentId,
      @Param("mentorId") Integer mentorId);

  @EntityGraph(
      attributePaths = {
        "assignment",
        "assignment.student",
        "assignment.student.user",
        "assignment.mentor",
        "assignment.mentor.user",
        "round",
        "criterion",
        "evaluatedBy"
      })
  Optional<AssessmentResult> findByAssignment_AssignmentIdAndRound_RoundIdAndCriterion_CriterionId(
      Integer assignmentId, Integer roundId, Integer criterionId);

  boolean existsByAssignment_AssignmentIdAndRound_RoundIdAndCriterion_CriterionId(
      Integer assignmentId, Integer roundId, Integer criterionId);
}
