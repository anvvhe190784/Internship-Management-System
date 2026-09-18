package com.example.internship.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class EvaluationCriteriaDTO {
    private Integer criterionId;

    @NotBlank(message = "Criterion name is required")
    private String criterionName;

    private String description;

    @NotNull(message = "Max score is required")
    @DecimalMin(value = "0.01", message = "Max score must be greater than 0")
    private BigDecimal maxScore;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
