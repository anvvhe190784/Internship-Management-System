package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.JwtResponse;
import com.example.internship.dto.LoginRequest;
import com.example.internship.dto.UserDTO;
import com.example.internship.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<JwtResponse>> login(
      @Valid @RequestBody LoginRequest loginRequest) {
    JwtResponse jwtResponse = authService.login(loginRequest);
    return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
    UserDTO userDTO = authService.getCurrentUser();
    return ResponseEntity.ok(ApiResponse.success(userDTO));
  }
}
