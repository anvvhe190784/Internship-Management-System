package com.example.internship.service;

import com.example.internship.dto.MentorDTO;
import com.example.internship.dto.UserDTO;
import com.example.internship.entity.Mentor;
import com.example.internship.entity.User;
import com.example.internship.entity.UserRole;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.MentorRepository;
import com.example.internship.repository.UserRepository;
import com.example.internship.security.UserPrincipal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentorService {

  private final MentorRepository mentorRepository;
  private final UserRepository userRepository;
  private final UserService userService;

  public MentorService(
      MentorRepository mentorRepository, UserRepository userRepository, UserService userService) {
    this.mentorRepository = mentorRepository;
    this.userRepository = userRepository;
    this.userService = userService;
  }

  @Transactional(readOnly = true)
  public List<MentorDTO> getAllMentors() {
    return mentorRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public MentorDTO getMentorById(Integer mentorId) {
    if (SecurityContextHolder.getContext().getAuthentication() != null
        && SecurityContextHolder.getContext().getAuthentication().getPrincipal()
            instanceof UserPrincipal current) {
      if (current.getRole() == UserRole.MENTOR && !current.getUserId().equals(mentorId)) {
        throw new AccessDeniedException("Mentors can only view their own profile");
      }
    }

    Mentor mentor =
        mentorRepository
            .findById(mentorId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Mentor profile not found for id: " + mentorId));
    return mapToDTO(mentor);
  }

  @Transactional
  public MentorDTO createMentor(MentorDTO dto) {
    if (dto.getMentorId() == null) {
      throw new BadRequestException("Mentor User ID is required");
    }

    User user =
        userRepository
            .findById(dto.getMentorId())
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found for id: " + dto.getMentorId()));

    if (user.getRole() != UserRole.MENTOR) {
      throw new BadRequestException("User must have MENTOR role to create mentor profile");
    }

    if (mentorRepository.existsById(dto.getMentorId())) {
      throw new DuplicateResourceException(
          "Mentor profile already exists for user id: " + dto.getMentorId());
    }

    Mentor mentor =
        Mentor.builder()
            .mentorId(user.getUserId())
            .user(user)
            .department(dto.getDepartment())
            .academicRank(dto.getAcademicRank())
            .build();

    return mapToDTO(mentorRepository.save(mentor));
  }

  @Transactional
  public MentorDTO updateMentor(Integer mentorId, MentorDTO dto) {
    UserPrincipal current = UserPrincipal.current();
    if (current.getRole() != UserRole.ADMIN
        && (!current.getRole().equals(UserRole.MENTOR) || !current.getUserId().equals(mentorId))) {
      throw new AccessDeniedException("You do not have permission to update this mentor profile");
    }

    Mentor mentor =
        mentorRepository
            .findById(mentorId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Mentor profile not found for id: " + mentorId));

    mentor.setDepartment(dto.getDepartment());
    mentor.setAcademicRank(dto.getAcademicRank());

    return mapToDTO(mentorRepository.save(mentor));
  }

  public MentorDTO mapToDTO(Mentor mentor) {
    UserDTO userDTO = userService.mapToDTO(mentor.getUser());
    return MentorDTO.builder()
        .mentorId(mentor.getMentorId())
        .user(userDTO)
        .department(mentor.getDepartment())
        .academicRank(mentor.getAcademicRank())
        .createdAt(mentor.getCreatedAt())
        .updatedAt(mentor.getUpdatedAt())
        .build();
  }
}
