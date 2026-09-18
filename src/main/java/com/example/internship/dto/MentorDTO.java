package com.example.internship.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

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
