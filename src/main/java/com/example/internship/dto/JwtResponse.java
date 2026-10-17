package com.example.internship.dto;

import com.example.internship.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
  private String token;
  @Builder.Default private String tokenType = "Bearer";
  private Integer userId;
  private String username;
  private String email;
  private UserRole role;
}
