package com.example.internship.service;

import com.example.internship.dto.AssessmentRoundDTO;
import com.example.internship.entity.AssessmentRound;
import com.example.internship.entity.InternshipPhase;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.AssessmentRoundRepository;
import com.example.internship.repository.InternshipPhaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentRoundService {

    private final AssessmentRoundRepository roundRepository;
    private final InternshipPhaseRepository phaseRepository;

    public AssessmentRoundService(AssessmentRoundRepository roundRepository, InternshipPhaseRepository phaseRepository) {
        this.roundRepository = roundRepository;
        this.phaseRepository = phaseRepository;
    }

    public List<AssessmentRoundDTO> getAllRounds(Integer phaseId) {
        List<AssessmentRound> rounds = (phaseId != null) ? roundRepository.findByPhase_PhaseId(phaseId) : roundRepository.findAll();
        return rounds.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public AssessmentRoundDTO getRoundById(Integer roundId) {
        AssessmentRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment round not found with id: " + roundId));
        return mapToDTO(round);
    }

    @Transactional
    public AssessmentRoundDTO createRound(AssessmentRoundDTO dto) {
        InternshipPhase phase = phaseRepository.findById(dto.getPhaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Internship phase not found with id: " + dto.getPhaseId()));

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("Round end date cannot be before start date");
        }

        AssessmentRound round = AssessmentRound.builder()
                .phase(phase)
                .roundName(dto.getRoundName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .description(dto.getDescription())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        return mapToDTO(roundRepository.save(round));
    }

    @Transactional
    public AssessmentRoundDTO updateRound(Integer roundId, AssessmentRoundDTO dto) {
        AssessmentRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment round not found with id: " + roundId));

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("Round end date cannot be before start date");
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
        AssessmentRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment round not found with id: " + roundId));
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
