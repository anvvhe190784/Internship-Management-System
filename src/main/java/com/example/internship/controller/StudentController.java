package com.example.internship.controller;

import com.example.internship.dto.ApiResponse;
import com.example.internship.dto.StudentDTO;
import com.example.internship.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getAllStudents() {
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentById(@PathVariable Integer studentId) {
        StudentDTO student = studentService.getStudentById(studentId);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO created = studentService.createStudent(studentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Student profile created", created));
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(@PathVariable Integer studentId, @Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO updated = studentService.updateStudent(studentId, studentDTO);
        return ResponseEntity.ok(ApiResponse.success("Student profile updated", updated));
    }
}
