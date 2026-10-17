package com.example.internship.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentRoundDTO {
  private Integer roundId;

  @NotNull(message = "Phase ID is required")
  private Integer phaseId;

  private String phaseName;

  @NotBlank(message = "Round name is required")
  private String roundName;

  @NotNull(message = "Start date is required")
  private LocalDate startDate;

  @NotNull(message = "End date is required")
  private LocalDate endDate;

  private String description;
  private Boolean isActive;
  private List<RoundCriteriaDTO> criteria;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
