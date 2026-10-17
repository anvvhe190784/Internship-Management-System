package com.example.internship.service;

import com.example.internship.dto.UserDTO;
import com.example.internship.entity.User;
import com.example.internship.entity.UserRole;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.UserRepository;
import com.example.internship.security.UserPrincipal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public List<UserDTO> getAllUsers(UserRole role) {
    List<User> users = (role != null) ? userRepository.findByRole(role) : userRepository.findAll();
    return users.stream().map(this::mapToDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public UserDTO getUserById(Integer userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    return mapToDTO(user);
  }

  @Transactional
  public UserDTO createUser(UserDTO dto) {
    if (userRepository.existsByUsername(dto.getUsername())) {
      throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
    }
    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
    }

    if (dto.getPassword() == null || dto.getPassword().isBlank()) {
      throw new BadRequestException("Password is required for new user creation");
    }

    User user =
        User.builder()
            .username(dto.getUsername())
            .passwordHash(passwordEncoder.encode(dto.getPassword()))
            .fullName(dto.getFullName())
            .email(dto.getEmail())
            .phoneNumber(dto.getPhoneNumber())
            .role(dto.getRole())
            .isActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE)
            .build();

    User savedUser = userRepository.save(user);
    return mapToDTO(savedUser);
  }

  @Transactional
  public UserDTO updateUser(Integer userId, UserDTO dto) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    if (!user.getEmail().equalsIgnoreCase(dto.getEmail())
        && userRepository.existsByEmail(dto.getEmail())) {
      throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
    }

    user.setFullName(dto.getFullName());
    user.setEmail(dto.getEmail());
    user.setPhoneNumber(dto.getPhoneNumber());

    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
    }

    User updatedUser = userRepository.save(user);
    return mapToDTO(updatedUser);
  }

  @Transactional
  public UserDTO updateStatus(Integer userId, Boolean isActive) {
    if (isActive == null) {
      throw new BadRequestException("isActive status is required");
    }
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    UserPrincipal current = UserPrincipal.current();
    if (user.getUserId().equals(current.getUserId())) {
      throw new AccessDeniedException("Admins cannot change their own active status");
    }
    if (user.getRole() == UserRole.ADMIN) {
      throw new AccessDeniedException("Admin users cannot modify status of another Admin");
    }

    user.setIsActive(isActive);
    return mapToDTO(userRepository.save(user));
  }

  @Transactional
  public UserDTO updateRole(Integer userId, UserRole newRole) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    UserPrincipal.current();
    if (user.getRole() == UserRole.ADMIN) {
      throw new AccessDeniedException("Admin users cannot modify role of an Admin");
    }

    user.setRole(newRole);
    return mapToDTO(userRepository.save(user));
  }

  @Transactional
  public void deleteUser(Integer userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    UserPrincipal current = UserPrincipal.current();
    if (user.getUserId().equals(current.getUserId()) || user.getRole() == UserRole.ADMIN) {
      throw new AccessDeniedException("Admin users cannot delete an Admin account");
    }

    userRepository.delete(user);
  }

  public UserDTO mapToDTO(User user) {
    return UserDTO.builder()
        .userId(user.getUserId())
        .username(user.getUsername())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .role(user.getRole())
        .isActive(user.getIsActive())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
