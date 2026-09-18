package com.example.internship.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResultDTO {
    private Integer resultId;

    @NotNull(message = "Assignment ID is required")
    private Integer assignmentId;

    @NotNull(message = "Round ID is required")
    private Integer roundId;
    private String roundName;

    @NotNull(message = "Criterion ID is required")
    private Integer criterionId;
    private String criterionName;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.00", message = "Score must be non-negative")
    private BigDecimal score;

    private String comments;
    private Integer evaluatedBy;
    private String evaluatorName;

    private OffsetDateTime evaluationDate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
