package com.example.internship.dto;

import com.example.internship.entity.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipAssignmentDTO {
    private Integer assignmentId;

    @NotNull(message = "Student ID is required")
    private Integer studentId;
    private String studentName;
    private String studentCode;

    @NotNull(message = "Mentor ID is required")
    private Integer mentorId;
    private String mentorName;

    @NotNull(message = "Phase ID is required")
    private Integer phaseId;
    private String phaseName;

    private OffsetDateTime assignedDate;
    private AssignmentStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
