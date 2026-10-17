package com.example.internship.service;

import com.example.internship.dto.InternshipAssignmentDTO;
import com.example.internship.entity.*;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.InvalidAssignmentStateException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.InternshipAssignmentRepository;
import com.example.internship.repository.InternshipPhaseRepository;
import com.example.internship.repository.MentorRepository;
import com.example.internship.repository.StudentRepository;
import com.example.internship.security.UserPrincipal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InternshipAssignmentService {

  private final InternshipAssignmentRepository assignmentRepository;
  private final StudentRepository studentRepository;
  private final MentorRepository mentorRepository;
  private final InternshipPhaseRepository phaseRepository;

  public InternshipAssignmentService(
      InternshipAssignmentRepository assignmentRepository,
      StudentRepository studentRepository,
      MentorRepository mentorRepository,
      InternshipPhaseRepository phaseRepository) {
    this.assignmentRepository = assignmentRepository;
    this.studentRepository = studentRepository;
    this.mentorRepository = mentorRepository;
    this.phaseRepository = phaseRepository;
  }

  @Transactional(readOnly = true)
  public List<InternshipAssignmentDTO> getAllAssignments(
      Integer studentId, Integer mentorId, Integer phaseId) {
    UserPrincipal current = UserPrincipal.current();

    if (current.getRole() == UserRole.STUDENT) {
      studentId = current.getUserId();
    } else if (current.getRole() == UserRole.MENTOR) {
      mentorId = current.getUserId();
    }

    List<InternshipAssignment> list =
        assignmentRepository.findWithFilters(studentId, mentorId, phaseId);
    return list.stream().map(this::mapToDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public InternshipAssignmentDTO getAssignmentById(Integer assignmentId) {
    InternshipAssignment assignment =
        assignmentRepository
            .findById(assignmentId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

    UserPrincipal current = UserPrincipal.current();
    if (current.getRole() == UserRole.STUDENT
        && !assignment.getStudent().getStudentId().equals(current.getUserId())) {
      throw new AccessDeniedException("Students can only view their own assignments");
    }
    if (current.getRole() == UserRole.MENTOR
        && !assignment.getMentor().getMentorId().equals(current.getUserId())) {
      throw new AccessDeniedException("Mentors can only view their assigned students");
    }

    return mapToDTO(assignment);
  }

  @Transactional
  public InternshipAssignmentDTO createAssignment(InternshipAssignmentDTO dto) {
    Student student =
        studentRepository
            .findById(dto.getStudentId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Student not found with id: " + dto.getStudentId()));

    Mentor mentor =
        mentorRepository
            .findById(dto.getMentorId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Mentor not found with id: " + dto.getMentorId()));

    InternshipPhase phase =
        phaseRepository
            .findById(dto.getPhaseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Internship phase not found with id: " + dto.getPhaseId()));

    if (assignmentRepository.existsByStudent_StudentIdAndPhase_PhaseId(
        dto.getStudentId(), dto.getPhaseId())) {
      throw new DuplicateResourceException("Student is already assigned to a mentor in this phase");
    }

    if (dto.getStatus() != null && dto.getStatus() != AssignmentStatus.PENDING) {
      throw new InvalidAssignmentStateException("New assignment must start in PENDING status");
    }

    InternshipAssignment assignment =
        InternshipAssignment.builder()
            .student(student)
            .mentor(mentor)
            .phase(phase)
            .status(AssignmentStatus.PENDING)
            .build();

    return mapToDTO(assignmentRepository.save(assignment));
  }

  @Transactional
  public InternshipAssignmentDTO updateStatus(Integer assignmentId, AssignmentStatus status) {
    if (status == null) {
      throw new BadRequestException("Status is required");
    }

    UserPrincipal current = UserPrincipal.current();
    if (current.getRole() != UserRole.ADMIN) {
      throw new AccessDeniedException("Only Admin is permitted to update assignment status");
    }

    InternshipAssignment assignment =
        assignmentRepository
            .findById(assignmentId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

    if (!assignment.getStatus().canTransitionTo(status)) {
      throw new InvalidAssignmentStateException(
          "Invalid assignment status transition from " + assignment.getStatus() + " to " + status);
    }

    assignment.setStatus(status);
    return mapToDTO(assignmentRepository.save(assignment));
  }

  public InternshipAssignmentDTO mapToDTO(InternshipAssignment assignment) {
    return InternshipAssignmentDTO.builder()
        .assignmentId(assignment.getAssignmentId())
        .studentId(assignment.getStudent().getStudentId())
        .studentName(assignment.getStudent().getUser().getFullName())
        .studentCode(assignment.getStudent().getStudentCode())
        .mentorId(assignment.getMentor().getMentorId())
        .mentorName(assignment.getMentor().getUser().getFullName())
        .phaseId(assignment.getPhase().getPhaseId())
        .phaseName(assignment.getPhase().getPhaseName())
        .assignedDate(assignment.getAssignedDate())
        .status(assignment.getStatus())
        .createdAt(assignment.getCreatedAt())
        .updatedAt(assignment.getUpdatedAt())
        .build();
  }
}
