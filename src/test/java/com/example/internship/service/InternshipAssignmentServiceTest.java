package com.example.internship.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.internship.dto.InternshipAssignmentDTO;
import com.example.internship.entity.*;
import com.example.internship.exception.InvalidAssignmentStateException;
import com.example.internship.repository.InternshipAssignmentRepository;
import com.example.internship.repository.InternshipPhaseRepository;
import com.example.internship.repository.MentorRepository;
import com.example.internship.repository.StudentRepository;
import com.example.internship.security.UserPrincipal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class InternshipAssignmentServiceTest {

  @Mock private InternshipAssignmentRepository assignmentRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private MentorRepository mentorRepository;
  @Mock private InternshipPhaseRepository phaseRepository;

  @InjectMocks private InternshipAssignmentService assignmentService;

  private Student student;
  private Mentor mentor;
  private InternshipPhase phase;
  private InternshipAssignment assignment;

  @BeforeEach
  void setUp() {
    User studentUser =
        User.builder()
            .userId(10)
            .username("student1")
            .fullName("Student One")
            .role(UserRole.STUDENT)
            .build();

    student = Student.builder().studentId(10).user(studentUser).studentCode("STU001").build();

    User mentorUser =
        User.builder()
            .userId(20)
            .username("mentor1")
            .fullName("Mentor One")
            .role(UserRole.MENTOR)
            .build();

    mentor = Mentor.builder().mentorId(20).user(mentorUser).department("IT").build();

    phase =
        InternshipPhase.builder()
            .phaseId(1)
            .phaseName("Phase 1")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2025, 11, 30))
            .build();

    assignment =
        InternshipAssignment.builder()
            .assignmentId(1)
            .student(student)
            .mentor(mentor)
            .phase(phase)
            .assignedDate(OffsetDateTime.now())
            .status(AssignmentStatus.PENDING)
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticate(Integer userId, String username, UserRole role) {
    UserPrincipal principal =
        UserPrincipal.builder()
            .userId(userId)
            .username(username)
            .role(role)
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())))
            .build();
    Authentication auth =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void updateStatus_StudentAttempt_ThrowsAccessDeniedException() {
    authenticate(10, "student1", UserRole.STUDENT);

    assertThatThrownBy(() -> assignmentService.updateStatus(1, AssignmentStatus.COMPLETED))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("Only Admin is permitted to update assignment status");
  }

  @Test
  void updateStatus_MentorAttempt_ThrowsAccessDeniedException() {
    authenticate(20, "mentor1", UserRole.MENTOR);

    assertThatThrownBy(() -> assignmentService.updateStatus(1, AssignmentStatus.IN_PROGRESS))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("Only Admin is permitted to update assignment status");
  }

  @Test
  void updateStatus_Admin_ValidTransition_Success() {
    authenticate(1, "admin", UserRole.ADMIN);

    when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
    when(assignmentRepository.save(any(InternshipAssignment.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // PENDING -> IN_PROGRESS
    InternshipAssignmentDTO updated =
        assignmentService.updateStatus(1, AssignmentStatus.IN_PROGRESS);

    assertThat(updated).isNotNull();
    assertThat(updated.getStatus()).isEqualTo(AssignmentStatus.IN_PROGRESS);

    // IN_PROGRESS -> COMPLETED
    assignment.setStatus(AssignmentStatus.IN_PROGRESS);
    InternshipAssignmentDTO completed =
        assignmentService.updateStatus(1, AssignmentStatus.COMPLETED);

    assertThat(completed).isNotNull();
    assertThat(completed.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
  }

  @Test
  void updateStatus_InvalidTransition_ThrowsInvalidAssignmentStateException() {
    authenticate(1, "admin", UserRole.ADMIN);

    when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));

    assertThatThrownBy(() -> assignmentService.updateStatus(1, AssignmentStatus.COMPLETED))
        .isInstanceOf(InvalidAssignmentStateException.class)
        .hasMessageContaining("Invalid assignment status transition");
  }

  @Test
  void getAssignmentById_StudentViewingOther_ThrowsAccessDeniedException() {
    authenticate(999, "other_student", UserRole.STUDENT);

    when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));

    assertThatThrownBy(() -> assignmentService.getAssignmentById(1))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("Students can only view their own assignments");
  }

  @Test
  void getAssignmentById_StudentViewingSelf_Success() {
    authenticate(10, "student1", UserRole.STUDENT);

    when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));

    InternshipAssignmentDTO result = assignmentService.getAssignmentById(1);

    assertThat(result).isNotNull();
    assertThat(result.getStudentId()).isEqualTo(10);
  }
}
