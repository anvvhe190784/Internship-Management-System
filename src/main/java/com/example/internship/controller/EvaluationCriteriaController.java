package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.EvaluationCriteriaDTO;
import com.example.internship.service.EvaluationCriteriaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluation_criteria")
public class EvaluationCriteriaController {

    private final EvaluationCriteriaService criteriaService;

    public EvaluationCriteriaController(EvaluationCriteriaService criteriaService) {
        this.criteriaService = criteriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluationCriteriaDTO>>> getAllCriteria() {
        List<EvaluationCriteriaDTO> list = criteriaService.getAllCriteria();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{criterionId}")
    public ResponseEntity<ApiResponse<EvaluationCriteriaDTO>> getCriterionById(
            @PathVariable Integer criterionId) {
        EvaluationCriteriaDTO dto = criteriaService.getCriterionById(criterionId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationCriteriaDTO>> createCriterion(
            @Valid @RequestBody EvaluationCriteriaDTO dto) {
        EvaluationCriteriaDTO created = criteriaService.createCriterion(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Evaluation criterion created", created));
    }

    @PutMapping("/{criterionId}")
    public ResponseEntity<ApiResponse<EvaluationCriteriaDTO>> updateCriterion(
            @PathVariable Integer criterionId, @Valid @RequestBody EvaluationCriteriaDTO dto) {
        EvaluationCriteriaDTO updated = criteriaService.updateCriterion(criterionId, dto);
        return ResponseEntity.ok(ApiResponse.success("Evaluation criterion updated", updated));
    }

    @DeleteMapping("/{criterionId}")
    public ResponseEntity<ApiResponse<Void>> deleteCriterion(@PathVariable Integer criterionId) {
        criteriaService.deleteCriterion(criterionId);
        return ResponseEntity.ok(ApiResponse.success("Evaluation criterion deleted", null));
    }
}
