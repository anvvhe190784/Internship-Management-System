package com.example.internship.service;

import com.example.internship.dto.InternshipPhaseDTO;
import com.example.internship.entity.InternshipPhase;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.InternshipPhaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InternshipPhaseService {

    private final InternshipPhaseRepository phaseRepository;

    public InternshipPhaseService(InternshipPhaseRepository phaseRepository) {
        this.phaseRepository = phaseRepository;
    }

    @Transactional(readOnly = true)
    public List<InternshipPhaseDTO> getAllPhases() {
        return phaseRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InternshipPhaseDTO getPhaseById(Integer phaseId) {
        InternshipPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship phase not found with id: " + phaseId));
        return mapToDTO(phase);
    }

    @Transactional
    public InternshipPhaseDTO createPhase(InternshipPhaseDTO dto) {
        if (phaseRepository.existsByPhaseName(dto.getPhaseName())) {
            throw new DuplicateResourceException("Internship phase name already exists: " + dto.getPhaseName());
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        InternshipPhase phase = InternshipPhase.builder()
                .phaseName(dto.getPhaseName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .description(dto.getDescription())
                .build();

        return mapToDTO(phaseRepository.save(phase));
    }

    @Transactional
    public InternshipPhaseDTO updatePhase(Integer phaseId, InternshipPhaseDTO dto) {
        InternshipPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship phase not found with id: " + phaseId));

        if (!phase.getPhaseName().equalsIgnoreCase(dto.getPhaseName())
                && phaseRepository.existsByPhaseName(dto.getPhaseName())) {
            throw new DuplicateResourceException("Internship phase name already exists: " + dto.getPhaseName());
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        phase.setPhaseName(dto.getPhaseName());
        phase.setStartDate(dto.getStartDate());
        phase.setEndDate(dto.getEndDate());
        phase.setDescription(dto.getDescription());

        return mapToDTO(phaseRepository.save(phase));
    }

    @Transactional
    public void deletePhase(Integer phaseId) {
        InternshipPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship phase not found with id: " + phaseId));
        phaseRepository.delete(phase);
    }

    public InternshipPhaseDTO mapToDTO(InternshipPhase phase) {
        return InternshipPhaseDTO.builder()
                .phaseId(phase.getPhaseId())
                .phaseName(phase.getPhaseName())
                .startDate(phase.getStartDate())
                .endDate(phase.getEndDate())
                .description(phase.getDescription())
                .createdAt(phase.getCreatedAt())
                .updatedAt(phase.getUpdatedAt())
                .build();
    }
}
