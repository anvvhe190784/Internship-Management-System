package com.example.internship.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorDTO {
  private Integer mentorId;
  private UserDTO user;
  private String department;
  private String academicRank;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
