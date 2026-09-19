package com.example.internship.repository;

import com.example.internship.entity.AssignmentStatus;
import com.example.internship.entity.InternshipAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InternshipAssignmentRepository extends JpaRepository<InternshipAssignment, Integer> {

    @Override
    @EntityGraph(attributePaths = { "student.user", "mentor.user", "phase" })
    List<InternshipAssignment> findAll();

    @Override
    @EntityGraph(attributePaths = { "student.user", "mentor.user", "phase" })
    Optional<InternshipAssignment> findById(Integer id);

    @EntityGraph(attributePaths = { "student.user", "mentor.user", "phase" })
    List<InternshipAssignment> findByStudent_StudentId(Integer studentId);

    @EntityGraph(attributePaths = { "student.user", "mentor.user", "phase" })
    List<InternshipAssignment> findByMentor_MentorId(Integer mentorId);

    @EntityGraph(attributePaths = { "student.user", "mentor.user", "phase" })
    List<InternshipAssignment> findByPhase_PhaseId(Integer phaseId);

    @Query("SELECT a FROM InternshipAssignment a " +
            "WHERE (:studentId IS NULL OR a.student.studentId = :studentId) " +
            "AND (:mentorId IS NULL OR a.mentor.mentorId = :mentorId) " +
            "AND (:phaseId IS NULL OR a.phase.phaseId = :phaseId)")
    @EntityGraph(attributePaths = { "student.user", "mentor.user", "phase" })
    List<InternshipAssignment> findWithFilters(@Param("studentId") Integer studentId,
            @Param("mentorId") Integer mentorId,
            @Param("phaseId") Integer phaseId);

    Optional<InternshipAssignment> findByStudent_StudentIdAndPhase_PhaseId(Integer studentId, Integer phaseId);

    boolean existsByStudent_StudentIdAndPhase_PhaseId(Integer studentId, Integer phaseId);

    @EntityGraph(attributePaths = { "student.user", "mentor.user", "phase" })
    List<InternshipAssignment> findByStatus(AssignmentStatus status);
}
