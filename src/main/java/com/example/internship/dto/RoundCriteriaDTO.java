package com.example.internship.dto;

import jakarta.validation.constraints.DecimalMax;
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
public class RoundCriteriaDTO {
    private Integer roundCriterionId;

    @NotNull(message = "Round ID is required")
    private Integer roundId;
    private String roundName;

    @NotNull(message = "Criterion ID is required")
    private Integer criterionId;
    private String criterionName;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @DecimalMax(value = "1.00", message = "Weight must not exceed 1.00")
    private BigDecimal weight;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
