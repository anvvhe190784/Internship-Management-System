package com.example.internship.repository;

import com.example.internship.entity.RoundCriteria;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoundCriteriaRepository extends JpaRepository<RoundCriteria, Integer> {

  @Override
  @EntityGraph(attributePaths = {"round", "criterion"})
  List<RoundCriteria> findAll();

  @Override
  @EntityGraph(attributePaths = {"round", "criterion"})
  Optional<RoundCriteria> findById(Integer id);

  @EntityGraph(attributePaths = {"round", "criterion"})
  List<RoundCriteria> findByRound_RoundId(Integer roundId);

  Optional<RoundCriteria> findByRound_RoundIdAndCriterion_CriterionId(
      Integer roundId, Integer criterionId);

  boolean existsByRound_RoundIdAndCriterion_CriterionId(Integer roundId, Integer criterionId);

  @Query(
      "SELECT COALESCE(SUM(rc.weight), 0) FROM RoundCriteria rc WHERE rc.round.roundId = :roundId"
          + " AND (:excludeId IS NULL OR rc.roundCriterionId <> :excludeId)")
  BigDecimal sumWeightByRoundId(
      @Param("roundId") Integer roundId, @Param("excludeId") Integer excludeId);
}
