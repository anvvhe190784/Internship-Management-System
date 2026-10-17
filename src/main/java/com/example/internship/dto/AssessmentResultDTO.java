package com.example.internship.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResultDTO {
  private Integer resultId;
  private Integer assignmentId;
  private Integer roundId;
  private String roundName;
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
