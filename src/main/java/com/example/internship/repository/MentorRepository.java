package com.example.internship.repository;

import com.example.internship.entity.Mentor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MentorRepository extends JpaRepository<Mentor, Integer> {

    @Override
    @EntityGraph(attributePaths = { "user" })
    List<Mentor> findAll();

    @Override
    @EntityGraph(attributePaths = { "user" })
    Optional<Mentor> findById(Integer id);
}
