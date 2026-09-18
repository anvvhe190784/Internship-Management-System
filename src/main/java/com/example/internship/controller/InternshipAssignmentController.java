package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.InternshipAssignmentDTO;
import com.example.internship.entity.AssignmentStatus;
import com.example.internship.service.InternshipAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internship_assignments")
public class InternshipAssignmentController {

    private final InternshipAssignmentService assignmentService;

    public InternshipAssignmentController(InternshipAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InternshipAssignmentDTO>>> getAllAssignments(
            @RequestParam(required = false) Integer student_id,
            @RequestParam(required = false) Integer mentor_id,
            @RequestParam(required = false) Integer phase_id) {
        List<InternshipAssignmentDTO> list = assignmentService.getAllAssignments(student_id, mentor_id, phase_id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<InternshipAssignmentDTO>> getAssignmentById(@PathVariable Integer assignmentId) {
        InternshipAssignmentDTO dto = assignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InternshipAssignmentDTO>> createAssignment(@Valid @RequestBody InternshipAssignmentDTO dto) {
        InternshipAssignmentDTO created = assignmentService.createAssignment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Internship assigned successfully", created));
    }

    @PutMapping("/{assignmentId}/status")
    public ResponseEntity<ApiResponse<InternshipAssignmentDTO>> updateStatus(@PathVariable Integer assignmentId, @RequestBody Map<String, String> body) {
        AssignmentStatus status = AssignmentStatus.valueOf(body.get("status"));
        InternshipAssignmentDTO updated = assignmentService.updateStatus(assignmentId, status);
        return ResponseEntity.ok(ApiResponse.success("Assignment status updated", updated));
    }
}
