package com.example.internship.repository;

import com.example.internship.entity.InternshipPhase;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternshipPhaseRepository extends JpaRepository<InternshipPhase, Integer> {
    Optional<InternshipPhase> findByPhaseName(String phaseName);

    boolean existsByPhaseName(String phaseName);
}
