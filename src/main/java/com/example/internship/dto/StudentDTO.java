package com.example.internship.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {
    private Integer studentId;
    private UserDTO user;

    @NotBlank(message = "Student code is required")
    private String studentCode;

    private String major;
    private String className;
    private LocalDate dateOfBirth;
    private String address;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
