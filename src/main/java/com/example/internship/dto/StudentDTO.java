package com.example.internship.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {
  private Integer studentId;
  private UserDTO user;
  private String studentCode;

  private String major;
  private String className;
  private LocalDate dateOfBirth;
  private String address;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
