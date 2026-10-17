package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.MentorDTO;
import com.example.internship.service.MentorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentors")
public class MentorController {

  private final MentorService mentorService;

  public MentorController(MentorService mentorService) {
    this.mentorService = mentorService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<MentorDTO>>> getAllMentors() {
    List<MentorDTO> mentors = mentorService.getAllMentors();
    return ResponseEntity.ok(ApiResponse.success(mentors));
  }

  @GetMapping("/{mentorId}")
  public ResponseEntity<ApiResponse<MentorDTO>> getMentorById(@PathVariable Integer mentorId) {
    MentorDTO mentor = mentorService.getMentorById(mentorId);
    return ResponseEntity.ok(ApiResponse.success(mentor));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<MentorDTO>> createMentor(
      @Valid @RequestBody MentorDTO mentorDTO) {
    MentorDTO created = mentorService.createMentor(mentorDTO);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Mentor profile created", created));
  }

  @PutMapping("/{mentorId}")
  public ResponseEntity<ApiResponse<MentorDTO>> updateMentor(
      @PathVariable Integer mentorId, @Valid @RequestBody MentorDTO mentorDTO) {
    MentorDTO updated = mentorService.updateMentor(mentorId, mentorDTO);
    return ResponseEntity.ok(ApiResponse.success("Mentor profile updated", updated));
  }
}
