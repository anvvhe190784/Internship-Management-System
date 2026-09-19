package com.example.internship.service;

import com.example.internship.dto.RoundCriteriaDTO;
import com.example.internship.entity.AssessmentRound;
import com.example.internship.entity.EvaluationCriteria;
import com.example.internship.entity.RoundCriteria;
import com.example.internship.exception.BadRequestException;
import com.example.internship.repository.AssessmentRoundRepository;
import com.example.internship.repository.EvaluationCriteriaRepository;
import com.example.internship.repository.RoundCriteriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoundCriteriaServiceTest {

        @Mock
        private RoundCriteriaRepository roundCriteriaRepository;
        @Mock
        private AssessmentRoundRepository roundRepository;
        @Mock
        private EvaluationCriteriaRepository criteriaRepository;

        @InjectMocks
        private RoundCriteriaService roundCriteriaService;

        private AssessmentRound round;
        private EvaluationCriteria criterion;

        @BeforeEach
        void setUp() {
                round = AssessmentRound.builder()
                                .roundId(1)
                                .roundName("Round 1")
                                .build();

                criterion = EvaluationCriteria.builder()
                                .criterionId(1)
                                .criterionName("Criterion 1")
                                .maxScore(new BigDecimal("10.00"))
                                .build();
        }

        @Test
        void addCriterionToRound_InvalidWeight_ThrowsBadRequestException() {
                RoundCriteriaDTO dto = RoundCriteriaDTO.builder()
                                .roundId(1)
                                .criterionId(1)
                                .weight(new BigDecimal("-0.10"))
                                .build();

                assertThatThrownBy(() -> roundCriteriaService.addCriterionToRound(dto))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Weight must be greater than 0");
        }

        @Test
        void addCriterionToRound_TotalWeightExceedsOne_ThrowsBadRequestException() {
                when(roundRepository.findById(1)).thenReturn(Optional.of(round));
                when(criteriaRepository.findById(1)).thenReturn(Optional.of(criterion));
                when(roundCriteriaRepository.existsByRound_RoundIdAndCriterion_CriterionId(1, 1)).thenReturn(false);

                // existing criteria in this round already sum to 0.70
                RoundCriteria existing = RoundCriteria.builder()
                                .roundCriterionId(10)
                                .weight(new BigDecimal("0.70"))
                                .build();
                when(roundCriteriaRepository.findByRound_RoundId(1)).thenReturn(List.of(existing));

                // trying to add 0.40 -> 0.70 + 0.40 = 1.10 > 1.00
                RoundCriteriaDTO dto = RoundCriteriaDTO.builder()
                                .roundId(1)
                                .criterionId(1)
                                .weight(new BigDecimal("0.40"))
                                .build();

                assertThatThrownBy(() -> roundCriteriaService.addCriterionToRound(dto))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Total weight of criteria for this round cannot exceed 1.00");
        }

        @Test
        void addCriterionToRound_ValidWeight_Success() {
                when(roundRepository.findById(1)).thenReturn(Optional.of(round));
                when(criteriaRepository.findById(1)).thenReturn(Optional.of(criterion));
                when(roundCriteriaRepository.existsByRound_RoundIdAndCriterion_CriterionId(1, 1)).thenReturn(false);

                // existing criteria in this round sum to 0.50
                RoundCriteria existing = RoundCriteria.builder()
                                .roundCriterionId(10)
                                .weight(new BigDecimal("0.50"))
                                .build();
                when(roundCriteriaRepository.findByRound_RoundId(1)).thenReturn(List.of(existing));

                RoundCriteria saved = RoundCriteria.builder()
                                .roundCriterionId(11)
                                .round(round)
                                .criterion(criterion)
                                .weight(new BigDecimal("0.40"))
                                .build();
                when(roundCriteriaRepository.save(any(RoundCriteria.class))).thenReturn(saved);

                RoundCriteriaDTO dto = RoundCriteriaDTO.builder()
                                .roundId(1)
                                .criterionId(1)
                                .weight(new BigDecimal("0.40"))
                                .build();

                RoundCriteriaDTO result = roundCriteriaService.addCriterionToRound(dto);

                assertThat(result).isNotNull();
                assertThat(result.getWeight()).isEqualByComparingTo("0.40");
        }
}
