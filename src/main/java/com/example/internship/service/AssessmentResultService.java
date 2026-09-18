package com.example.internship.service;

import com.example.internship.dto.AssessmentResultDTO;
import com.example.internship.entity.*;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.*;
import com.example.internship.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentResultService {

    private final AssessmentResultRepository resultRepository;
    private final InternshipAssignmentRepository assignmentRepository;
    private final AssessmentRoundRepository roundRepository;
    private final EvaluationCriteriaRepository criteriaRepository;
    private final UserRepository userRepository;

    public AssessmentResultService(AssessmentResultRepository resultRepository, InternshipAssignmentRepository assignmentRepository, AssessmentRoundRepository roundRepository, EvaluationCriteriaRepository criteriaRepository, UserRepository userRepository) {
        this.resultRepository = resultRepository;
        this.assignmentRepository = assignmentRepository;
        this.roundRepository = roundRepository;
        this.criteriaRepository = criteriaRepository;
        this.userRepository = userRepository;
    }

    public List<AssessmentResultDTO> getResults(Integer assignmentId, Integer roundId) {
        UserPrincipal current = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<AssessmentResult> list;
        if (assignmentId != null && roundId != null) {
            list = resultRepository.findByAssignment_AssignmentIdAndRound_RoundId(assignmentId, roundId);
        } else if (assignmentId != null) {
            list = resultRepository.findByAssignment_AssignmentId(assignmentId);
        } else {
            list = resultRepository.findAll();
        }

        if (current.getRole() == UserRole.STUDENT) {
            list = list.stream()
                    .filter(r -> r.getAssignment().getStudent().getStudentId().equals(current.getUserId()))
                    .collect(Collectors.toList());
        } else if (current.getRole() == UserRole.MENTOR) {
            list = list.stream()
                    .filter(r -> r.getAssignment().getMentor().getMentorId().equals(current.getUserId()))
                    .collect(Collectors.toList());
        }

        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public AssessmentResultDTO getResultById(Integer resultId) {
        AssessmentResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment result not found with id: " + resultId));

        UserPrincipal current = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (current.getRole() == UserRole.STUDENT && !result.getAssignment().getStudent().getStudentId().equals(current.getUserId())) {
            throw new BadRequestException("Students can only view their own assessment results");
        }
        if (current.getRole() == UserRole.MENTOR && !result.getAssignment().getMentor().getMentorId().equals(current.getUserId())) {
            throw new BadRequestException("Mentors can only view assessment results of their assigned students");
        }

        return mapToDTO(result);
    }

    @Transactional
    public AssessmentResultDTO evaluateCriterion(AssessmentResultDTO dto) {
        UserPrincipal current = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InternshipAssignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + dto.getAssignmentId()));

        if (current.getRole() == UserRole.MENTOR && !assignment.getMentor().getMentorId().equals(current.getUserId())) {
            throw new BadRequestException("Mentors can only evaluate students assigned to them");
        }

        AssessmentRound round = roundRepository.findById(dto.getRoundId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment round not found with id: " + dto.getRoundId()));

        EvaluationCriteria criterion = criteriaRepository.findById(dto.getCriterionId())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation criteria not found with id: " + dto.getCriterionId()));

        if (dto.getScore().compareTo(criterion.getMaxScore()) > 0) {
            throw new BadRequestException("Score (" + dto.getScore() + ") cannot exceed maximum score (" + criterion.getMaxScore() + ")");
        }

        if (resultRepository.existsByAssignment_AssignmentIdAndRound_RoundIdAndCriterion_CriterionId(
                dto.getAssignmentId(), dto.getRoundId(), dto.getCriterionId())) {
            throw new DuplicateResourceException("This criterion has already been evaluated for this student in this round");
        }

        User evaluator = userRepository.findById(current.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator user not found"));

        AssessmentResult result = AssessmentResult.builder()
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
        UserPrincipal current = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        AssessmentResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment result not found with id: " + resultId));

        if (current.getRole() == UserRole.MENTOR && !result.getAssignment().getMentor().getMentorId().equals(current.getUserId())) {
            throw new BadRequestException("Mentors can only update evaluations for their assigned students");
        }

        if (dto.getScore().compareTo(result.getCriterion().getMaxScore()) > 0) {
            throw new BadRequestException("Score cannot exceed maximum score (" + result.getCriterion().getMaxScore() + ")");
        }

        result.setScore(dto.getScore());
        result.setComments(dto.getComments());

        return mapToDTO(resultRepository.save(result));
    }

    @Transactional
    public void deleteResult(Integer resultId) {
        AssessmentResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment result not found with id: " + resultId));
        resultRepository.delete(result);
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
