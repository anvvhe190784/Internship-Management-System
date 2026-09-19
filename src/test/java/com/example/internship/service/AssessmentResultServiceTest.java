package com.example.internship.service;

import com.example.internship.dto.AssessmentResultDTO;
import com.example.internship.entity.*;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.repository.*;
import com.example.internship.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentResultServiceTest {

        @Mock
        private AssessmentResultRepository resultRepository;
        @Mock
        private InternshipAssignmentRepository assignmentRepository;
        @Mock
        private AssessmentRoundRepository roundRepository;
        @Mock
        private EvaluationCriteriaRepository criteriaRepository;
        @Mock
        private RoundCriteriaRepository roundCriteriaRepository;
        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private AssessmentResultService assessmentResultService;

        private InternshipPhase phase;
        private AssessmentRound round;
        private EvaluationCriteria criterion;
        private Student student;
        private Mentor mentor;
        private InternshipAssignment assignment;
        private User mentorUser;
        private User studentUser;

        @BeforeEach
        void setUp() {
                phase = InternshipPhase.builder()
                                .phaseId(1)
                                .phaseName("Phase 1")
                                .startDate(LocalDate.of(2025, 9, 1))
                                .endDate(LocalDate.of(2025, 11, 30))
                                .build();

                round = AssessmentRound.builder()
                                .roundId(1)
                                .phase(phase)
                                .roundName("Midterm")
                                .startDate(LocalDate.of(2025, 10, 1))
                                .endDate(LocalDate.of(2025, 10, 15))
                                .build();

                criterion = EvaluationCriteria.builder()
                                .criterionId(1)
                                .criterionName("Technical Skills")
                                .maxScore(new BigDecimal("10.00"))
                                .build();

                studentUser = User.builder()
                                .userId(10)
                                .username("student1")
                                .fullName("Student One")
                                .role(UserRole.STUDENT)
                                .build();

                student = Student.builder()
                                .studentId(10)
                                .user(studentUser)
                                .studentCode("STU001")
                                .build();

                mentorUser = User.builder()
                                .userId(20)
                                .username("mentor1")
                                .fullName("Mentor One")
                                .role(UserRole.MENTOR)
                                .build();

                mentor = Mentor.builder()
                                .mentorId(20)
                                .user(mentorUser)
                                .department("IT")
                                .build();

                assignment = InternshipAssignment.builder()
                                .assignmentId(1)
                                .student(student)
                                .mentor(mentor)
                                .phase(phase)
                                .status(AssignmentStatus.IN_PROGRESS)
                                .build();
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        private void authenticate(Integer userId, String username, UserRole role) {
                UserPrincipal principal = UserPrincipal.builder()
                                .userId(userId)
                                .username(username)
                                .role(role)
                                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())))
                                .build();
                Authentication auth = new UsernamePasswordAuthenticationToken(principal, null,
                                principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @Test
        void evaluateCriterion_StudentAttempt_ThrowsAccessDeniedException() {
                authenticate(10, "student1", UserRole.STUDENT);

                AssessmentResultDTO dto = AssessmentResultDTO.builder()
                                .assignmentId(1)
                                .roundId(1)
                                .criterionId(1)
                                .score(new BigDecimal("9.50"))
                                .build();

                assertThatThrownBy(() -> assessmentResultService.evaluateCriterion(dto))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("Students are not permitted");

                verifyNoInteractions(resultRepository);
        }

        @Test
        void evaluateCriterion_MismatchedMentor_ThrowsAccessDeniedException() {
                authenticate(99, "other_mentor", UserRole.MENTOR);

                when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));

                AssessmentResultDTO dto = AssessmentResultDTO.builder()
                                .assignmentId(1)
                                .roundId(1)
                                .criterionId(1)
                                .score(new BigDecimal("9.00"))
                                .build();

                assertThatThrownBy(() -> assessmentResultService.evaluateCriterion(dto))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("Mentors can only evaluate students assigned to them");
        }

        @Test
        void evaluateCriterion_RoundMismatchPhase_ThrowsBadRequestException() {
                authenticate(20, "mentor1", UserRole.MENTOR);

                InternshipPhase differentPhase = InternshipPhase.builder()
                                .phaseId(2)
                                .phaseName("Phase 2")
                                .build();

                AssessmentRound mismatchRound = AssessmentRound.builder()
                                .roundId(2)
                                .phase(differentPhase)
                                .roundName("Round Phase 2")
                                .build();

                when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
                when(roundRepository.findById(2)).thenReturn(Optional.of(mismatchRound));

                AssessmentResultDTO dto = AssessmentResultDTO.builder()
                                .assignmentId(1)
                                .roundId(2)
                                .criterionId(1)
                                .score(new BigDecimal("9.00"))
                                .build();

                assertThatThrownBy(() -> assessmentResultService.evaluateCriterion(dto))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("does not belong to the internship phase");
        }

        @Test
        void evaluateCriterion_CriterionNotInRound_ThrowsBadRequestException() {
                authenticate(20, "mentor1", UserRole.MENTOR);

                when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
                when(roundRepository.findById(1)).thenReturn(Optional.of(round));
                when(criteriaRepository.findById(1)).thenReturn(Optional.of(criterion));
                when(roundCriteriaRepository.existsByRound_RoundIdAndCriterion_CriterionId(1, 1)).thenReturn(false);

                AssessmentResultDTO dto = AssessmentResultDTO.builder()
                                .assignmentId(1)
                                .roundId(1)
                                .criterionId(1)
                                .score(new BigDecimal("9.00"))
                                .build();

                assertThatThrownBy(() -> assessmentResultService.evaluateCriterion(dto))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Criterion is not configured for this assessment round");
        }

        @Test
        void evaluateCriterion_ScoreExceedsMaxScore_ThrowsBadRequestException() {
                authenticate(20, "mentor1", UserRole.MENTOR);

                when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
                when(roundRepository.findById(1)).thenReturn(Optional.of(round));
                when(criteriaRepository.findById(1)).thenReturn(Optional.of(criterion));
                when(roundCriteriaRepository.existsByRound_RoundIdAndCriterion_CriterionId(1, 1)).thenReturn(true);

                AssessmentResultDTO dto = AssessmentResultDTO.builder()
                                .assignmentId(1)
                                .roundId(1)
                                .criterionId(1)
                                .score(new BigDecimal("11.00")) // max is 10.00
                                .build();

                assertThatThrownBy(() -> assessmentResultService.evaluateCriterion(dto))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("cannot exceed maximum score");
        }

        @Test
        void evaluateCriterion_DuplicateEvaluation_ThrowsDuplicateResourceException() {
                authenticate(20, "mentor1", UserRole.MENTOR);

                when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
                when(roundRepository.findById(1)).thenReturn(Optional.of(round));
                when(criteriaRepository.findById(1)).thenReturn(Optional.of(criterion));
                when(roundCriteriaRepository.existsByRound_RoundIdAndCriterion_CriterionId(1, 1)).thenReturn(true);
                when(resultRepository.existsByAssignment_AssignmentIdAndRound_RoundIdAndCriterion_CriterionId(1, 1, 1))
                                .thenReturn(true);

                AssessmentResultDTO dto = AssessmentResultDTO.builder()
                                .assignmentId(1)
                                .roundId(1)
                                .criterionId(1)
                                .score(new BigDecimal("9.00"))
                                .build();

                assertThatThrownBy(() -> assessmentResultService.evaluateCriterion(dto))
                                .isInstanceOf(DuplicateResourceException.class)
                                .hasMessageContaining("already been evaluated");
        }

        @Test
        void evaluateCriterion_ValidMentor_Success() {
                authenticate(20, "mentor1", UserRole.MENTOR);

                when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
                when(roundRepository.findById(1)).thenReturn(Optional.of(round));
                when(criteriaRepository.findById(1)).thenReturn(Optional.of(criterion));
                when(roundCriteriaRepository.existsByRound_RoundIdAndCriterion_CriterionId(1, 1)).thenReturn(true);
                when(resultRepository.existsByAssignment_AssignmentIdAndRound_RoundIdAndCriterion_CriterionId(1, 1, 1))
                                .thenReturn(false);
                when(userRepository.findById(20)).thenReturn(Optional.of(mentorUser));

                AssessmentResult saved = AssessmentResult.builder()
                                .resultId(100)
                                .assignment(assignment)
                                .round(round)
                                .criterion(criterion)
                                .score(new BigDecimal("9.00"))
                                .comments("Great job")
                                .evaluatedBy(mentorUser)
                                .build();

                when(resultRepository.save(any(AssessmentResult.class))).thenReturn(saved);

                AssessmentResultDTO dto = AssessmentResultDTO.builder()
                                .assignmentId(1)
                                .roundId(1)
                                .criterionId(1)
                                .score(new BigDecimal("9.00"))
                                .comments("Great job")
                                .build();

                AssessmentResultDTO result = assessmentResultService.evaluateCriterion(dto);

                assertThat(result).isNotNull();
                assertThat(result.getResultId()).isEqualTo(100);
                assertThat(result.getScore()).isEqualByComparingTo("9.00");
                assertThat(result.getEvaluatorName()).isEqualTo("Mentor One");
        }

        @Test
        void updateEvaluation_StudentAttempt_ThrowsAccessDeniedException() {
                authenticate(10, "student1", UserRole.STUDENT);

                AssessmentResultDTO dto = AssessmentResultDTO.builder()
                                .score(new BigDecimal("10.00"))
                                .build();

                assertThatThrownBy(() -> assessmentResultService.updateEvaluation(100, dto))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("Students are not permitted");
        }
}
