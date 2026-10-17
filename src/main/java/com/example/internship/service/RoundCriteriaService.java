package com.example.internship.service;

import com.example.internship.dto.RoundCriteriaDTO;
import com.example.internship.entity.AssessmentRound;
import com.example.internship.entity.EvaluationCriteria;
import com.example.internship.entity.RoundCriteria;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.AssessmentRoundRepository;
import com.example.internship.repository.EvaluationCriteriaRepository;
import com.example.internship.repository.RoundCriteriaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoundCriteriaService {

  private final RoundCriteriaRepository roundCriteriaRepository;
  private final AssessmentRoundRepository roundRepository;
  private final EvaluationCriteriaRepository criteriaRepository;

  public RoundCriteriaService(
      RoundCriteriaRepository roundCriteriaRepository,
      AssessmentRoundRepository roundRepository,
      EvaluationCriteriaRepository criteriaRepository) {
    this.roundCriteriaRepository = roundCriteriaRepository;
    this.roundRepository = roundRepository;
    this.criteriaRepository = criteriaRepository;
  }

  @Transactional(readOnly = true)
  public List<RoundCriteriaDTO> getCriteriaByRound(Integer roundId) {
    List<RoundCriteria> list =
        (roundId != null)
            ? roundCriteriaRepository.findByRound_RoundId(roundId)
            : roundCriteriaRepository.findAll();
    return list.stream().map(this::mapToDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public RoundCriteriaDTO getRoundCriterionById(Integer roundCriterionId) {
    RoundCriteria rc =
        roundCriteriaRepository
            .findById(roundCriterionId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Round criterion not found with id: " + roundCriterionId));
    return mapToDTO(rc);
  }

  @Transactional
  public RoundCriteriaDTO addCriterionToRound(RoundCriteriaDTO dto) {
    if (dto.getRoundId() == null) {
      throw new BadRequestException("Round ID is required");
    }
    if (dto.getCriterionId() == null) {
      throw new BadRequestException("Criterion ID is required");
    }

    if (dto.getWeight() == null
        || dto.getWeight().compareTo(BigDecimal.ZERO) <= 0
        || dto.getWeight().compareTo(BigDecimal.ONE) > 0) {
      throw new BadRequestException("Weight must be greater than 0 and at most 1.00");
    }

    AssessmentRound round =
        roundRepository
            .findById(dto.getRoundId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Assessment round not found with id: " + dto.getRoundId()));

    EvaluationCriteria criterion =
        criteriaRepository
            .findById(dto.getCriterionId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Evaluation criteria not found with id: " + dto.getCriterionId()));

    if (roundCriteriaRepository.existsByRound_RoundIdAndCriterion_CriterionId(
        dto.getRoundId(), dto.getCriterionId())) {
      throw new DuplicateResourceException("Criterion already added to this assessment round");
    }

    BigDecimal sum = roundCriteriaRepository.sumWeightByRoundId(dto.getRoundId(), null);
    BigDecimal currentTotal = sum != null ? sum : BigDecimal.ZERO;

    if (currentTotal.add(dto.getWeight()).compareTo(BigDecimal.ONE) > 0) {
      throw new BadRequestException(
          "Total weight of criteria for this round cannot exceed 1.00 (Current total: "
              + currentTotal
              + ")");
    }

    RoundCriteria rc =
        RoundCriteria.builder().round(round).criterion(criterion).weight(dto.getWeight()).build();

    return mapToDTO(roundCriteriaRepository.save(rc));
  }

  @Transactional
  public RoundCriteriaDTO updateWeight(Integer roundCriterionId, RoundCriteriaDTO dto) {
    if (dto.getWeight() == null
        || dto.getWeight().compareTo(BigDecimal.ZERO) <= 0
        || dto.getWeight().compareTo(BigDecimal.ONE) > 0) {
      throw new BadRequestException("Weight must be greater than 0 and at most 1.00");
    }

    RoundCriteria rc =
        roundCriteriaRepository
            .findById(roundCriterionId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Round criterion not found with id: " + roundCriterionId));

    BigDecimal sum =
        roundCriteriaRepository.sumWeightByRoundId(rc.getRound().getRoundId(), roundCriterionId);
    BigDecimal currentTotalWithoutThis = sum != null ? sum : BigDecimal.ZERO;

    if (currentTotalWithoutThis.add(dto.getWeight()).compareTo(BigDecimal.ONE) > 0) {
      throw new BadRequestException(
          "Total weight of criteria for this round cannot exceed 1.00 (Current total: "
              + currentTotalWithoutThis
              + ")");
    }

    rc.setWeight(dto.getWeight());
    return mapToDTO(roundCriteriaRepository.save(rc));
  }

  @Transactional
  public void deleteRoundCriterion(Integer roundCriterionId) {
    RoundCriteria rc =
        roundCriteriaRepository
            .findById(roundCriterionId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Round criterion not found with id: " + roundCriterionId));
    roundCriteriaRepository.delete(rc);
  }

  public RoundCriteriaDTO mapToDTO(RoundCriteria rc) {
    return RoundCriteriaDTO.builder()
        .roundCriterionId(rc.getRoundCriterionId())
        .roundId(rc.getRound().getRoundId())
        .roundName(rc.getRound().getRoundName())
        .criterionId(rc.getCriterion().getCriterionId())
        .criterionName(rc.getCriterion().getCriterionName())
        .weight(rc.getWeight())
        .createdAt(rc.getCreatedAt())
        .updatedAt(rc.getUpdatedAt())
        .build();
  }
}
