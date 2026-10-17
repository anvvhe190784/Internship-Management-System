package com.example.internship.service;

import com.example.internship.dto.AssessmentRoundDTO;
import com.example.internship.dto.RoundCriteriaDTO;
import com.example.internship.entity.AssessmentRound;
import com.example.internship.entity.EvaluationCriteria;
import com.example.internship.entity.InternshipPhase;
import com.example.internship.entity.RoundCriteria;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.AssessmentRoundRepository;
import com.example.internship.repository.EvaluationCriteriaRepository;
import com.example.internship.repository.InternshipPhaseRepository;
import com.example.internship.repository.RoundCriteriaRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssessmentRoundService {

  private final AssessmentRoundRepository roundRepository;
  private final InternshipPhaseRepository phaseRepository;
  private final RoundCriteriaRepository roundCriteriaRepository;
  private final EvaluationCriteriaRepository criteriaRepository;

  public AssessmentRoundService(
      AssessmentRoundRepository roundRepository,
      InternshipPhaseRepository phaseRepository,
      RoundCriteriaRepository roundCriteriaRepository,
      EvaluationCriteriaRepository criteriaRepository) {
    this.roundRepository = roundRepository;
    this.phaseRepository = phaseRepository;
    this.roundCriteriaRepository = roundCriteriaRepository;
    this.criteriaRepository = criteriaRepository;
  }

  @Transactional(readOnly = true)
  public List<AssessmentRoundDTO> getAllRounds(Integer phaseId) {
    List<AssessmentRound> rounds =
        (phaseId != null)
            ? roundRepository.findByPhase_PhaseId(phaseId)
            : roundRepository.findAll();
    return rounds.stream().map(this::mapToDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public AssessmentRoundDTO getRoundById(Integer roundId) {
    AssessmentRound round =
        roundRepository
            .findById(roundId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Assessment round not found with id: " + roundId));
    return mapToDTO(round);
  }

  @Transactional
  public AssessmentRoundDTO createRound(AssessmentRoundDTO dto) {
    InternshipPhase phase =
        phaseRepository
            .findById(dto.getPhaseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Internship phase not found with id: " + dto.getPhaseId()));

    if (dto.getEndDate().isBefore(dto.getStartDate())) {
      throw new BadRequestException("Round end date cannot be before start date");
    }

    if (dto.getStartDate().isBefore(phase.getStartDate())
        || dto.getEndDate().isAfter(phase.getEndDate())) {
      throw new BadRequestException(
          "Round dates must be within phase period ("
              + phase.getStartDate()
              + " to "
              + phase.getEndDate()
              + ")");
    }

    AssessmentRound round =
        AssessmentRound.builder()
            .phase(phase)
            .roundName(dto.getRoundName())
            .startDate(dto.getStartDate())
            .endDate(dto.getEndDate())
            .description(dto.getDescription())
            .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
            .build();

    AssessmentRound savedRound = roundRepository.save(round);

    if (dto.getCriteria() != null && !dto.getCriteria().isEmpty()) {
      BigDecimal totalWeight = BigDecimal.ZERO;
      Set<Integer> seenCriterionIds = new HashSet<>();
      for (RoundCriteriaDTO c : dto.getCriteria()) {
        if (c.getCriterionId() == null) {
          throw new BadRequestException("Criterion ID is required");
        }
        if (!seenCriterionIds.add(c.getCriterionId())) {
          throw new DuplicateResourceException(
              "Duplicate criterion id in initial criteria: " + c.getCriterionId());
        }
        if (c.getWeight() == null
            || c.getWeight().compareTo(BigDecimal.ZERO) <= 0
            || c.getWeight().compareTo(BigDecimal.ONE) > 0) {
          throw new BadRequestException("Weight must be greater than 0 and at most 1.00");
        }
        totalWeight = totalWeight.add(c.getWeight());
        if (totalWeight.compareTo(BigDecimal.ONE) > 0) {
          throw new BadRequestException("Total criteria weight cannot exceed 1.00");
        }
        EvaluationCriteria ec =
            criteriaRepository
                .findById(c.getCriterionId())
                .orElseThrow(
                    () ->
                        new ResourceNotFoundException(
                            "Evaluation criteria not found with id: " + c.getCriterionId()));
        RoundCriteria rc =
            RoundCriteria.builder().round(savedRound).criterion(ec).weight(c.getWeight()).build();
        roundCriteriaRepository.save(rc);
      }
    }

    return mapToDTO(savedRound);
  }

  @Transactional
  public AssessmentRoundDTO updateRound(Integer roundId, AssessmentRoundDTO dto) {
    AssessmentRound round =
        roundRepository
            .findById(roundId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Assessment round not found with id: " + roundId));

    if (dto.getEndDate().isBefore(dto.getStartDate())) {
      throw new BadRequestException("Round end date cannot be before start date");
    }

    InternshipPhase phase = round.getPhase();
    if (dto.getStartDate().isBefore(phase.getStartDate())
        || dto.getEndDate().isAfter(phase.getEndDate())) {
      throw new BadRequestException(
          "Round dates must be within phase period ("
              + phase.getStartDate()
              + " to "
              + phase.getEndDate()
              + ")");
    }

    round.setRoundName(dto.getRoundName());
    round.setStartDate(dto.getStartDate());
    round.setEndDate(dto.getEndDate());
    round.setDescription(dto.getDescription());
    if (dto.getIsActive() != null) {
      round.setIsActive(dto.getIsActive());
    }

    return mapToDTO(roundRepository.save(round));
  }

  @Transactional
  public void deleteRound(Integer roundId) {
    AssessmentRound round =
        roundRepository
            .findById(roundId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Assessment round not found with id: " + roundId));
    roundRepository.delete(round);
  }

  public AssessmentRoundDTO mapToDTO(AssessmentRound round) {
    return AssessmentRoundDTO.builder()
        .roundId(round.getRoundId())
        .phaseId(round.getPhase().getPhaseId())
        .phaseName(round.getPhase().getPhaseName())
        .roundName(round.getRoundName())
        .startDate(round.getStartDate())
        .endDate(round.getEndDate())
        .description(round.getDescription())
        .isActive(round.getIsActive())
        .createdAt(round.getCreatedAt())
        .updatedAt(round.getUpdatedAt())
        .build();
  }
}
