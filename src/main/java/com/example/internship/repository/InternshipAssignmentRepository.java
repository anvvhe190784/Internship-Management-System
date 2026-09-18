package com.example.internship.repository;

import com.example.internship.entity.AssignmentStatus;
import com.example.internship.entity.InternshipAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InternshipAssignmentRepository extends JpaRepository<InternshipAssignment, Integer> {
    List<InternshipAssignment> findByStudent_StudentId(Integer studentId);
    List<InternshipAssignment> findByMentor_MentorId(Integer mentorId);
    List<InternshipAssignment> findByPhase_PhaseId(Integer phaseId);
    Optional<InternshipAssignment> findByStudent_StudentIdAndPhase_PhaseId(Integer studentId, Integer phaseId);
    boolean existsByStudent_StudentIdAndPhase_PhaseId(Integer studentId, Integer phaseId);
    List<InternshipAssignment> findByStatus(AssignmentStatus status);
}
