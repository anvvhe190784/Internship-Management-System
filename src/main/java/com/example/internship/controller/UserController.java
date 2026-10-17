package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.UserDTO;
import com.example.internship.entity.UserRole;
import com.example.internship.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  public record StatusUpdateRequest(@NotNull(message = "isActive is required") Boolean isActive) {}

  public record RoleUpdateRequest(@NotNull(message = "role is required") UserRole role) {}

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
      @RequestParam(required = false) UserRole role) {
    List<UserDTO> users = userService.getAllUsers(role);
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Integer userId) {
    UserDTO user = userService.getUserById(userId);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO userDTO) {
    UserDTO created = userService.createUser(userDTO);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "User created successfully", created));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<ApiResponse<UserDTO>> updateUser(
      @PathVariable Integer userId, @Valid @RequestBody UserDTO userDTO) {
    UserDTO updated = userService.updateUser(userId, userDTO);
    return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
  }

  @PutMapping("/{userId}/status")
  public ResponseEntity<ApiResponse<UserDTO>> updateStatus(
      @PathVariable Integer userId, @Valid @RequestBody StatusUpdateRequest request) {
    UserDTO updated = userService.updateStatus(userId, request.isActive());
    return ResponseEntity.ok(ApiResponse.success("User status updated", updated));
  }

  @PutMapping("/{userId}/role")
  public ResponseEntity<ApiResponse<UserDTO>> updateRole(
      @PathVariable Integer userId, @Valid @RequestBody RoleUpdateRequest request) {
    UserDTO updated = userService.updateRole(userId, request.role());
    return ResponseEntity.ok(ApiResponse.success("User role updated", updated));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer userId) {
    userService.deleteUser(userId);
    return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
  }
}
