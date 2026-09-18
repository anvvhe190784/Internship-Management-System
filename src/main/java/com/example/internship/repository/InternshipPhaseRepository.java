package com.example.internship.repository;

import com.example.internship.entity.InternshipPhase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InternshipPhaseRepository extends JpaRepository<InternshipPhase, Integer> {
    Optional<InternshipPhase> findByPhaseName(String phaseName);
    boolean existsByPhaseName(String phaseName);
}
