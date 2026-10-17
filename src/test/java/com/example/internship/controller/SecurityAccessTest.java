package com.example.internship.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.internship.config.SecurityConfig;
import com.example.internship.security.CustomUserDetailsService;
import com.example.internship.security.JwtAuthenticationFilter;
import com.example.internship.security.JwtTokenProvider;
import com.example.internship.service.AssessmentResultService;
import com.example.internship.service.AuthService;
import com.example.internship.service.StudentService;
import com.example.internship.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitConfig(
    classes = {
      SecurityConfig.class,
      JwtAuthenticationFilter.class,
      StudentController.class,
      AssessmentResultController.class,
      UserController.class,
      AuthController.class,
      SecurityAccessTest.TestMvcConfig.class
    })
@WebAppConfiguration
class SecurityAccessTest {

  @Autowired private WebApplicationContext context;
  private MockMvc mockMvc;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private CustomUserDetailsService customUserDetailsService;
  @MockitoBean private StudentService studentService;
  @MockitoBean private AssessmentResultService assessmentResultService;
  @MockitoBean private UserService userService;
  @MockitoBean private AuthService authService;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
  }

  @Test
  void unauthenticatedRequest_Returns401Json() throws Exception {
    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.status_code").value(401))
        .andExpect(jsonPath("$.error_code").value("INVALID_JWT_TOKEN"))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unauthorized")));
  }

  @Test
  void unauthenticatedGetMe_Returns401Unauthorized() throws Exception {
    mockMvc
        .perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.status_code").value(401))
        .andExpect(jsonPath("$.error_code").value("INVALID_JWT_TOKEN"))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unauthorized")));
  }

  @Test
  void studentCallingGetAllStudents_Returns403Forbidden() throws Exception {
    mockMvc
        .perform(get("/api/students").with(user("student").roles("STUDENT")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.status_code").value(403))
        .andExpect(jsonPath("$.error_code").value("ACCESS_DENIED"))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Access denied")));
  }

  @Test
  void studentCallingEvaluateAssessment_Returns403Forbidden() throws Exception {
    mockMvc
        .perform(
            post("/api/assessment_results")
                .with(user("student").roles("STUDENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"assignmentId\":1,\"roundId\":1,\"criterionId\":1,\"score\":9.5}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.status_code").value(403))
        .andExpect(jsonPath("$.error_code").value("ACCESS_DENIED"))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Access denied")));
  }

  @Test
  void adminCallingGetAllStudents_Returns200Ok() throws Exception {
    when(studentService.getAllStudents()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/students").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Configuration
  @EnableWebMvc
  static class TestMvcConfig {}
}
