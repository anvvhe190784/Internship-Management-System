package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.UserDTO;
import com.example.internship.entity.UserRole;
import com.example.internship.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(@RequestParam(required = false) UserRole role) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User created successfully", created));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Integer userId, @Valid @RequestBody UserDTO userDTO) {
        UserDTO updated = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserDTO>> updateStatus(@PathVariable Integer userId, @RequestBody Map<String, Boolean> body) {
        Boolean isActive = body.get("isActive");
        UserDTO updated = userService.updateStatus(userId, isActive != null ? isActive : true);
        return ResponseEntity.ok(ApiResponse.success("User status updated", updated));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserDTO>> updateRole(@PathVariable Integer userId, @RequestBody Map<String, String> body) {
        UserRole role = UserRole.valueOf(body.get("role"));
        UserDTO updated = userService.updateRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated", updated));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
