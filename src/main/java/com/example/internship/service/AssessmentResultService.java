package com.example.internship.service;

import com.example.internship.dto.AssessmentResultDTO;
import com.example.internship.entity.*;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.InvalidAssignmentStateException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.*;
import com.example.internship.security.UserPrincipal;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssessmentResultService {

  private final AssessmentResultRepository resultRepository;
  private final InternshipAssignmentRepository assignmentRepository;
  private final AssessmentRoundRepository roundRepository;
  private final EvaluationCriteriaRepository criteriaRepository;
  private final RoundCriteriaRepository roundCriteriaRepository;
  private final UserRepository userRepository;

  public AssessmentResultService(
      AssessmentResultRepository resultRepository,
      InternshipAssignmentRepository assignmentRepository,
      AssessmentRoundRepository roundRepository,
      EvaluationCriteriaRepository criteriaRepository,
      RoundCriteriaRepository roundCriteriaRepository,
      UserRepository userRepository) {
    this.resultRepository = resultRepository;
    this.assignmentRepository = assignmentRepository;
    this.roundRepository = roundRepository;
    this.criteriaRepository = criteriaRepository;
    this.roundCriteriaRepository = roundCriteriaRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<AssessmentResultDTO> getResults(Integer assignmentId, Integer roundId) {
    UserPrincipal current = UserPrincipal.current();

    Integer studentFilter = null;
    Integer mentorFilter = null;

    if (current.getRole() == UserRole.STUDENT) {
      studentFilter = current.getUserId();
    } else if (current.getRole() == UserRole.MENTOR) {
      mentorFilter = current.getUserId();
    }

    List<AssessmentResult> list =
        resultRepository.findWithFilters(assignmentId, roundId, studentFilter, mentorFilter);
    return list.stream().map(this::mapToDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public AssessmentResultDTO getResultById(Integer resultId) {
    AssessmentResult result =
        resultRepository
            .findById(resultId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Assessment result not found with id: " + resultId));

    UserPrincipal current = UserPrincipal.current();
    if (current.getRole() == UserRole.STUDENT
        && !result.getAssignment().getStudent().getStudentId().equals(current.getUserId())) {
      throw new AccessDeniedException("Students can only view their own assessment results");
    }
    if (current.getRole() == UserRole.MENTOR
        && !result.getAssignment().getMentor().getMentorId().equals(current.getUserId())) {
      throw new AccessDeniedException(
          "Mentors can only view assessment results of their assigned students");
    }

    return mapToDTO(result);
  }

  @Transactional
  public AssessmentResultDTO evaluateCriterion(AssessmentResultDTO dto) {
    UserPrincipal current = UserPrincipal.current();

    if (current.getRole() != UserRole.MENTOR) {
      throw new AccessDeniedException("Only Mentors are permitted to evaluate assessments");
    }

    if (dto.getAssignmentId() == null) {
      throw new BadRequestException("Assignment ID is required");
    }
    if (dto.getRoundId() == null) {
      throw new BadRequestException("Round ID is required");
    }
    if (dto.getCriterionId() == null) {
      throw new BadRequestException("Criterion ID is required");
    }

    if (dto.getScore() == null || dto.getScore().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException("Score must be non-negative");
    }

    InternshipAssignment assignment =
        assignmentRepository
            .findById(dto.getAssignmentId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Assignment not found with id: " + dto.getAssignmentId()));

    if (!assignment.getMentor().getMentorId().equals(current.getUserId())) {
      throw new AccessDeniedException("Mentors can only evaluate students assigned to them");
    }

    if (assignment.getStatus() != AssignmentStatus.IN_PROGRESS) {
      throw new InvalidAssignmentStateException(
          "Cannot evaluate assignment in status: " + assignment.getStatus());
    }

    AssessmentRound round =
        roundRepository
            .findById(dto.getRoundId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Assessment round not found with id: " + dto.getRoundId()));

    if (Boolean.FALSE.equals(round.getIsActive())) {
      throw new BadRequestException("Cannot evaluate an inactive assessment round");
    }

    if (!round.getPhase().getPhaseId().equals(assignment.getPhase().getPhaseId())) {
      throw new BadRequestException(
          "Assessment round does not belong to the internship phase of this assignment");
    }

    EvaluationCriteria criterion =
        criteriaRepository
            .findById(dto.getCriterionId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Evaluation criteria not found with id: " + dto.getCriterionId()));

    if (!roundCriteriaRepository.existsByRound_RoundIdAndCriterion_CriterionId(
        dto.getRoundId(), dto.getCriterionId())) {
      throw new BadRequestException("Criterion is not configured for this assessment round");
    }

    if (dto.getScore().compareTo(criterion.getMaxScore()) > 0) {
      throw new BadRequestException(
          "Score ("
              + dto.getScore()
              + ") cannot exceed maximum score ("
              + criterion.getMaxScore()
              + ")");
    }

    if (resultRepository.existsByAssignment_AssignmentIdAndRound_RoundIdAndCriterion_CriterionId(
        dto.getAssignmentId(), dto.getRoundId(), dto.getCriterionId())) {
      throw new DuplicateResourceException(
          "This criterion has already been evaluated for this student in this round");
    }

    User evaluator =
        userRepository
            .findById(current.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Evaluator user not found"));

    AssessmentResult result =
        AssessmentResult.builder()
            .assignment(assignment)
            .round(round)
            .criterion(criterion)
            .score(dto.getScore())
            .comments(dto.getComments())
            .evaluatedBy(evaluator)
            .build();

    return mapToDTO(resultRepository.save(result));
  }

  @Transactional
  public AssessmentResultDTO updateEvaluation(Integer resultId, AssessmentResultDTO dto) {
    UserPrincipal current = UserPrincipal.current();

    if (current.getRole() != UserRole.MENTOR) {
      throw new AccessDeniedException("Only Mentors are permitted to update assessments");
    }

    if (dto.getScore() == null || dto.getScore().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException("Score must be non-negative");
    }

    AssessmentResult result =
        resultRepository
            .findById(resultId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Assessment result not found with id: " + resultId));

    if (!result.getAssignment().getMentor().getMentorId().equals(current.getUserId())
        || (result.getEvaluatedBy() != null
            && !result.getEvaluatedBy().getUserId().equals(current.getUserId()))) {
      throw new AccessDeniedException(
          "Mentors can only update evaluations created by themselves for their assigned students");
    }

    if (result.getAssignment().getStatus() != AssignmentStatus.IN_PROGRESS) {
      throw new InvalidAssignmentStateException(
          "Cannot evaluate assignment in status: " + result.getAssignment().getStatus());
    }

    if (Boolean.FALSE.equals(result.getRound().getIsActive())) {
      throw new BadRequestException("Cannot update evaluation in an inactive assessment round");
    }

    if (dto.getScore().compareTo(result.getCriterion().getMaxScore()) > 0) {
      throw new BadRequestException(
          "Score cannot exceed maximum score (" + result.getCriterion().getMaxScore() + ")");
    }

    result.setScore(dto.getScore());
    result.setComments(dto.getComments());

    return mapToDTO(resultRepository.save(result));
  }

  public AssessmentResultDTO mapToDTO(AssessmentResult result) {
    return AssessmentResultDTO.builder()
        .resultId(result.getResultId())
        .assignmentId(result.getAssignment().getAssignmentId())
        .roundId(result.getRound().getRoundId())
        .roundName(result.getRound().getRoundName())
        .criterionId(result.getCriterion().getCriterionId())
        .criterionName(result.getCriterion().getCriterionName())
        .score(result.getScore())
        .comments(result.getComments())
        .evaluatedBy(result.getEvaluatedBy().getUserId())
        .evaluatorName(result.getEvaluatedBy().getFullName())
        .evaluationDate(result.getEvaluationDate())
        .createdAt(result.getCreatedAt())
        .updatedAt(result.getUpdatedAt())
        .build();
  }
}
