package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.RoundCriteriaDTO;
import com.example.internship.service.RoundCriteriaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/round_criteria")
public class RoundCriteriaController {

  private final RoundCriteriaService roundCriteriaService;

  public RoundCriteriaController(RoundCriteriaService roundCriteriaService) {
    this.roundCriteriaService = roundCriteriaService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<RoundCriteriaDTO>>> getCriteriaByRound(
      @RequestParam(required = false) Integer round_id) {
    List<RoundCriteriaDTO> list = roundCriteriaService.getCriteriaByRound(round_id);
    return ResponseEntity.ok(ApiResponse.success(list));
  }

  @GetMapping("/{roundCriterionId}")
  public ResponseEntity<ApiResponse<RoundCriteriaDTO>> getRoundCriterionById(
      @PathVariable Integer roundCriterionId) {
    RoundCriteriaDTO dto = roundCriteriaService.getRoundCriterionById(roundCriterionId);
    return ResponseEntity.ok(ApiResponse.success(dto));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<RoundCriteriaDTO>> addCriterionToRound(
      @Valid @RequestBody RoundCriteriaDTO dto) {
    RoundCriteriaDTO created = roundCriteriaService.addCriterionToRound(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Criterion added to round", created));
  }

  @PutMapping("/{roundCriterionId}")
  public ResponseEntity<ApiResponse<RoundCriteriaDTO>> updateWeight(
      @PathVariable Integer roundCriterionId, @Valid @RequestBody RoundCriteriaDTO dto) {
    RoundCriteriaDTO updated = roundCriteriaService.updateWeight(roundCriterionId, dto);
    return ResponseEntity.ok(ApiResponse.success("Criterion weight updated", updated));
  }

  @DeleteMapping("/{roundCriterionId}")
  public ResponseEntity<ApiResponse<Void>> deleteRoundCriterion(
      @PathVariable Integer roundCriterionId) {
    roundCriteriaService.deleteRoundCriterion(roundCriterionId);
    return ResponseEntity.ok(ApiResponse.success("Criterion removed from round", null));
  }
}
