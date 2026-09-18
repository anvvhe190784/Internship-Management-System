package com.example.internship.service;

import com.example.internship.dto.EvaluationCriteriaDTO;
import com.example.internship.entity.EvaluationCriteria;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.EvaluationCriteriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationCriteriaService {

    private final EvaluationCriteriaRepository criteriaRepository;

    public EvaluationCriteriaService(EvaluationCriteriaRepository criteriaRepository) {
        this.criteriaRepository = criteriaRepository;
    }

    public List<EvaluationCriteriaDTO> getAllCriteria() {
        return criteriaRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public EvaluationCriteriaDTO getCriterionById(Integer criterionId) {
        EvaluationCriteria criteria = criteriaRepository.findById(criterionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation criteria not found with id: " + criterionId));
        return mapToDTO(criteria);
    }

    @Transactional
    public EvaluationCriteriaDTO createCriterion(EvaluationCriteriaDTO dto) {
        if (criteriaRepository.existsByCriterionName(dto.getCriterionName())) {
            throw new DuplicateResourceException("Criterion name already exists: " + dto.getCriterionName());
        }

        EvaluationCriteria criteria = EvaluationCriteria.builder()
                .criterionName(dto.getCriterionName())
                .description(dto.getDescription())
                .maxScore(dto.getMaxScore())
                .build();

        return mapToDTO(criteriaRepository.save(criteria));
    }

    @Transactional
    public EvaluationCriteriaDTO updateCriterion(Integer criterionId, EvaluationCriteriaDTO dto) {
        EvaluationCriteria criteria = criteriaRepository.findById(criterionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation criteria not found with id: " + criterionId));

        if (!criteria.getCriterionName().equalsIgnoreCase(dto.getCriterionName()) && criteriaRepository.existsByCriterionName(dto.getCriterionName())) {
            throw new DuplicateResourceException("Criterion name already exists: " + dto.getCriterionName());
        }

        criteria.setCriterionName(dto.getCriterionName());
        criteria.setDescription(dto.getDescription());
        criteria.setMaxScore(dto.getMaxScore());

        return mapToDTO(criteriaRepository.save(criteria));
    }

    @Transactional
    public void deleteCriterion(Integer criterionId) {
        EvaluationCriteria criteria = criteriaRepository.findById(criterionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation criteria not found with id: " + criterionId));
        criteriaRepository.delete(criteria);
    }

    public EvaluationCriteriaDTO mapToDTO(EvaluationCriteria criteria) {
        return EvaluationCriteriaDTO.builder()
                .criterionId(criteria.getCriterionId())
                .criterionName(criteria.getCriterionName())
                .description(criteria.getDescription())
                .maxScore(criteria.getMaxScore())
                .createdAt(criteria.getCreatedAt())
                .updatedAt(criteria.getUpdatedAt())
                .build();
    }
}
