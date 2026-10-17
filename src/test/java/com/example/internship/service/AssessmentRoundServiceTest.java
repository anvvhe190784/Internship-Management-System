package com.example.internship.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.internship.dto.AssessmentRoundDTO;
import com.example.internship.entity.AssessmentRound;
import com.example.internship.entity.InternshipPhase;
import com.example.internship.exception.BadRequestException;
import com.example.internship.repository.AssessmentRoundRepository;
import com.example.internship.repository.EvaluationCriteriaRepository;
import com.example.internship.repository.InternshipPhaseRepository;
import com.example.internship.repository.RoundCriteriaRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssessmentRoundServiceTest {

  @Mock private AssessmentRoundRepository roundRepository;
  @Mock private InternshipPhaseRepository phaseRepository;
  @Mock private RoundCriteriaRepository roundCriteriaRepository;
  @Mock private EvaluationCriteriaRepository criteriaRepository;

  @InjectMocks private AssessmentRoundService roundService;

  private InternshipPhase phase;

  @BeforeEach
  void setUp() {
    phase =
        InternshipPhase.builder()
            .phaseId(1)
            .phaseName("Phase 2025")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2025, 11, 30))
            .build();
  }

  @Test
  void createRound_DateBeforePhaseStart_ThrowsBadRequestException() {
    when(phaseRepository.findById(1)).thenReturn(Optional.of(phase));

    AssessmentRoundDTO dto =
        AssessmentRoundDTO.builder()
            .phaseId(1)
            .roundName("Early Round")
            .startDate(LocalDate.of(2025, 8, 15)) // before phase start (Sep 1)
            .endDate(LocalDate.of(2025, 9, 15))
            .build();

    assertThatThrownBy(() -> roundService.createRound(dto))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Round dates must be within phase period");
  }

  @Test
  void createRound_DateAfterPhaseEnd_ThrowsBadRequestException() {
    when(phaseRepository.findById(1)).thenReturn(Optional.of(phase));

    AssessmentRoundDTO dto =
        AssessmentRoundDTO.builder()
            .phaseId(1)
            .roundName("Late Round")
            .startDate(LocalDate.of(2025, 11, 1))
            .endDate(LocalDate.of(2025, 12, 10)) // after phase end (Nov 30)
            .build();

    assertThatThrownBy(() -> roundService.createRound(dto))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Round dates must be within phase period");
  }

  @Test
  void createRound_EndDateBeforeStartDate_ThrowsBadRequestException() {
    when(phaseRepository.findById(1)).thenReturn(Optional.of(phase));

    AssessmentRoundDTO dto =
        AssessmentRoundDTO.builder()
            .phaseId(1)
            .roundName("Backwards Round")
            .startDate(LocalDate.of(2025, 10, 15))
            .endDate(LocalDate.of(2025, 10, 10))
            .build();

    assertThatThrownBy(() -> roundService.createRound(dto))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Round end date cannot be before start date");
  }

  @Test
  void createRound_ValidDatesWithinPhase_Success() {
    when(phaseRepository.findById(1)).thenReturn(Optional.of(phase));

    AssessmentRound saved =
        AssessmentRound.builder()
            .roundId(10)
            .phase(phase)
            .roundName("Midterm")
            .startDate(LocalDate.of(2025, 10, 1))
            .endDate(LocalDate.of(2025, 10, 15))
            .isActive(true)
            .build();

    when(roundRepository.save(any(AssessmentRound.class))).thenReturn(saved);

    AssessmentRoundDTO dto =
        AssessmentRoundDTO.builder()
            .phaseId(1)
            .roundName("Midterm")
            .startDate(LocalDate.of(2025, 10, 1))
            .endDate(LocalDate.of(2025, 10, 15))
            .build();

    AssessmentRoundDTO result = roundService.createRound(dto);

    assertThat(result).isNotNull();
    assertThat(result.getRoundId()).isEqualTo(10);
    assertThat(result.getRoundName()).isEqualTo("Midterm");
  }
}
