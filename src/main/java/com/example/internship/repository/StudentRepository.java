package com.example.internship.repository;

import com.example.internship.entity.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Integer> {

  @Override
  @EntityGraph(attributePaths = {"user"})
  List<Student> findAll();

  @Override
  @EntityGraph(attributePaths = {"user"})
  Optional<Student> findById(Integer id);

  @EntityGraph(attributePaths = {"user"})
  Optional<Student> findByStudentCode(String studentCode);

  boolean existsByStudentCode(String studentCode);

  @EntityGraph(attributePaths = {"user"})
  @Query(
      "SELECT DISTINCT a.student FROM InternshipAssignment a WHERE a.mentor.mentorId = :mentorId")
  List<Student> findAssignedStudentsByMentorId(@Param("mentorId") Integer mentorId);
}
