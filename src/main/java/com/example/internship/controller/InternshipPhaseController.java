package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.InternshipPhaseDTO;
import com.example.internship.service.InternshipPhaseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internship_phases")
public class InternshipPhaseController {

    private final InternshipPhaseService phaseService;

    public InternshipPhaseController(InternshipPhaseService phaseService) {
        this.phaseService = phaseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InternshipPhaseDTO>>> getAllPhases() {
        List<InternshipPhaseDTO> phases = phaseService.getAllPhases();
        return ResponseEntity.ok(ApiResponse.success(phases));
    }

    @GetMapping("/{phaseId}")
    public ResponseEntity<ApiResponse<InternshipPhaseDTO>> getPhaseById(
            @PathVariable Integer phaseId) {
        InternshipPhaseDTO phase = phaseService.getPhaseById(phaseId);
        return ResponseEntity.ok(ApiResponse.success(phase));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InternshipPhaseDTO>> createPhase(
            @Valid @RequestBody InternshipPhaseDTO dto) {
        InternshipPhaseDTO created = phaseService.createPhase(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Internship phase created", created));
    }

    @PutMapping("/{phaseId}")
    public ResponseEntity<ApiResponse<InternshipPhaseDTO>> updatePhase(
            @PathVariable Integer phaseId, @Valid @RequestBody InternshipPhaseDTO dto) {
        InternshipPhaseDTO updated = phaseService.updatePhase(phaseId, dto);
        return ResponseEntity.ok(ApiResponse.success("Internship phase updated", updated));
    }

    @DeleteMapping("/{phaseId}")
    public ResponseEntity<ApiResponse<Void>> deletePhase(@PathVariable Integer phaseId) {
        phaseService.deletePhase(phaseId);
        return ResponseEntity.ok(ApiResponse.success("Internship phase deleted", null));
    }
}
