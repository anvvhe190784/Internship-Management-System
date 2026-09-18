package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.AssessmentResultDTO;
import com.example.internship.service.AssessmentResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessment_results")
public class AssessmentResultController {

    private final AssessmentResultService resultService;

    public AssessmentResultController(AssessmentResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssessmentResultDTO>>> getResults(
            @RequestParam(required = false) Integer assignment_id,
            @RequestParam(required = false) Integer round_id) {
        List<AssessmentResultDTO> list = resultService.getResults(assignment_id, round_id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<ApiResponse<AssessmentResultDTO>> getResultById(@PathVariable Integer resultId) {
        AssessmentResultDTO dto = resultService.getResultById(resultId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AssessmentResultDTO>> evaluateCriterion(@Valid @RequestBody AssessmentResultDTO dto) {
        AssessmentResultDTO created = resultService.evaluateCriterion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Criterion evaluation saved", created));
    }

    @PutMapping("/{resultId}")
    public ResponseEntity<ApiResponse<AssessmentResultDTO>> updateEvaluation(@PathVariable Integer resultId, @Valid @RequestBody AssessmentResultDTO dto) {
        AssessmentResultDTO updated = resultService.updateEvaluation(resultId, dto);
        return ResponseEntity.ok(ApiResponse.success("Criterion evaluation updated", updated));
    }

    @DeleteMapping("/{resultId}")
    public ResponseEntity<ApiResponse<Void>> deleteResult(@PathVariable Integer resultId) {
        resultService.deleteResult(resultId);
        return ResponseEntity.ok(ApiResponse.success("Assessment result deleted", null));
    }
}
