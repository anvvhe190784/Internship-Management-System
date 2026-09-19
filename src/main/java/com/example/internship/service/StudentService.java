package com.example.internship.service;

import com.example.internship.dto.StudentDTO;
import com.example.internship.dto.UserDTO;
import com.example.internship.entity.Student;
import com.example.internship.entity.User;
import com.example.internship.entity.UserRole;
import com.example.internship.exception.BadRequestException;
import com.example.internship.exception.DuplicateResourceException;
import com.example.internship.exception.ResourceNotFoundException;
import com.example.internship.repository.StudentRepository;
import com.example.internship.repository.UserRepository;
import com.example.internship.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public StudentService(StudentRepository studentRepository, UserRepository userRepository, UserService userService) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentDTO getStudentById(Integer studentId) {
        UserPrincipal current = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (current.getRole() == UserRole.STUDENT && !current.getUserId().equals(studentId)) {
            throw new AccessDeniedException("Students can only view their own profile");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for id: " + studentId));
        return mapToDTO(student);
    }

    @Transactional
    public StudentDTO createStudent(StudentDTO dto) {
        if (dto.getStudentId() == null) {
            throw new BadRequestException("Student User ID is required");
        }

        User user = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id: " + dto.getStudentId()));

        if (user.getRole() != UserRole.STUDENT) {
            throw new BadRequestException("User must have STUDENT role to create student profile");
        }

        if (studentRepository.existsByStudentCode(dto.getStudentCode())) {
            throw new DuplicateResourceException("Student code already exists: " + dto.getStudentCode());
        }

        Student student = Student.builder()
                .studentId(user.getUserId())
                .user(user)
                .studentCode(dto.getStudentCode())
                .major(dto.getMajor())
                .className(dto.getClassName())
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .build();

        return mapToDTO(studentRepository.save(student));
    }

    @Transactional
    public StudentDTO updateStudent(Integer studentId, StudentDTO dto) {
        UserPrincipal current = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (current.getRole() != UserRole.ADMIN
                && (!current.getRole().equals(UserRole.STUDENT) || !current.getUserId().equals(studentId))) {
            throw new AccessDeniedException("You do not have permission to update this student profile");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for id: " + studentId));

        if (!student.getStudentCode().equalsIgnoreCase(dto.getStudentCode())
                && studentRepository.existsByStudentCode(dto.getStudentCode())) {
            throw new DuplicateResourceException("Student code already exists: " + dto.getStudentCode());
        }

        student.setStudentCode(dto.getStudentCode());
        student.setMajor(dto.getMajor());
        student.setClassName(dto.getClassName());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setAddress(dto.getAddress());

        return mapToDTO(studentRepository.save(student));
    }

    public StudentDTO mapToDTO(Student student) {
        UserDTO userDTO = userService.mapToDTO(student.getUser());
        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .user(userDTO)
                .studentCode(student.getStudentCode())
                .major(student.getMajor())
                .className(student.getClassName())
                .dateOfBirth(student.getDateOfBirth())
                .address(student.getAddress())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
