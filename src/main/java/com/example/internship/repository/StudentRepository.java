package com.example.internship.repository;

import com.example.internship.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    @Override
    @EntityGraph(attributePaths = { "user" })
    List<Student> findAll();

    @Override
    @EntityGraph(attributePaths = { "user" })
    Optional<Student> findById(Integer id);

    @EntityGraph(attributePaths = { "user" })
    Optional<Student> findByStudentCode(String studentCode);

    boolean existsByStudentCode(String studentCode);
}
