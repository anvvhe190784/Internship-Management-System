package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.AssessmentRoundDTO;
import com.example.internship.service.AssessmentRoundService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment_rounds")
public class AssessmentRoundController {

  private final AssessmentRoundService roundService;

  public AssessmentRoundController(AssessmentRoundService roundService) {
    this.roundService = roundService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<AssessmentRoundDTO>>> getAllRounds(
      @RequestParam(required = false) Integer phase_id) {
    List<AssessmentRoundDTO> rounds = roundService.getAllRounds(phase_id);
    return ResponseEntity.ok(ApiResponse.success(rounds));
  }

  @GetMapping("/{roundId}")
  public ResponseEntity<ApiResponse<AssessmentRoundDTO>> getRoundById(
      @PathVariable Integer roundId) {
    AssessmentRoundDTO round = roundService.getRoundById(roundId);
    return ResponseEntity.ok(ApiResponse.success(round));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<AssessmentRoundDTO>> createRound(
      @Valid @RequestBody AssessmentRoundDTO dto) {
    AssessmentRoundDTO created = roundService.createRound(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Assessment round created", created));
  }

  @PutMapping("/{roundId}")
  public ResponseEntity<ApiResponse<AssessmentRoundDTO>> updateRound(
      @PathVariable Integer roundId, @Valid @RequestBody AssessmentRoundDTO dto) {
    AssessmentRoundDTO updated = roundService.updateRound(roundId, dto);
    return ResponseEntity.ok(ApiResponse.success("Assessment round updated", updated));
  }

  @DeleteMapping("/{roundId}")
  public ResponseEntity<ApiResponse<Void>> deleteRound(@PathVariable Integer roundId) {
    roundService.deleteRound(roundId);
    return ResponseEntity.ok(ApiResponse.success("Assessment round deleted", null));
  }
}
