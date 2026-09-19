package com.example.internship.config;

import com.example.internship.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        exceptions -> exceptions
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {
                                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                            String msg = authException.getMessage() != null
                                                    ? authException.getMessage().replace("\"", "\\\"")
                                                    : "Authentication required";
                                            response
                                                    .getWriter()
                                                    .write(
                                                            "{\"success\":false,\"message\":\"Unauthorized: "
                                                                    + msg
                                                                    + "\",\"data\":null}");
                                        })
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) -> {
                                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                            response
                                                    .getWriter()
                                                    .write(
                                                            "{\"success\":false,\"message\":\"Access denied: You do not have"
                                                                    + " permission to access this resource\",\"data\":null}");
                                        }))
                .authorizeHttpRequests(
                        auth -> auth
                                // Public auth endpoints
                                .requestMatchers("/api/auth/**")
                                .permitAll()

                                // User management: Admin only
                                .requestMatchers("/api/users/**")
                                .hasRole("ADMIN")

                                // Students: Only Admin and Mentor can view all students. Student can view
                                // individual profile
                                .requestMatchers(HttpMethod.GET, "/api/students")
                                .hasAnyRole("ADMIN", "MENTOR")
                                .requestMatchers(HttpMethod.GET, "/api/students/**")
                                .hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                                .requestMatchers(HttpMethod.POST, "/api/students/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/students/**")
                                .hasAnyRole("ADMIN", "STUDENT")

                                // Mentors
                                .requestMatchers(HttpMethod.GET, "/api/mentors/**")
                                .hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                                .requestMatchers(HttpMethod.POST, "/api/mentors/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/mentors/**")
                                .hasAnyRole("ADMIN", "MENTOR")

                                // Phases, Criteria, Rounds, Round Criteria: Read for all, Write for Admin
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/internship_phases/**",
                                        "/api/evaluation_criteria/**",
                                        "/api/assessment_rounds/**",
                                        "/api/round_criteria/**")
                                .hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/internship_phases/**",
                                        "/api/evaluation_criteria/**",
                                        "/api/assessment_rounds/**",
                                        "/api/round_criteria/**")
                                .hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/internship_phases/**",
                                        "/api/evaluation_criteria/**",
                                        "/api/assessment_rounds/**",
                                        "/api/round_criteria/**")
                                .hasRole("ADMIN")

                                // Internship Assignments: Read for all, Assign for Admin, Status update for
                                // Admin & Mentor
                                .requestMatchers(HttpMethod.GET, "/api/internship_assignments/**")
                                .hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                                .requestMatchers(HttpMethod.POST, "/api/internship_assignments/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/internship_assignments/**")
                                .hasAnyRole("ADMIN", "MENTOR")

                                // Assessment Results: Read for all, Evaluate and Update for Admin & Mentor ONLY
                                .requestMatchers(HttpMethod.GET, "/api/assessment_results/**")
                                .hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                                .requestMatchers(HttpMethod.POST, "/api/assessment_results/**")
                                .hasAnyRole("ADMIN", "MENTOR")
                                .requestMatchers(HttpMethod.PUT, "/api/assessment_results/**")
                                .hasAnyRole("ADMIN", "MENTOR")

                                // Delete operations: Admin only
                                .requestMatchers(HttpMethod.DELETE, "/api/**")
                                .hasRole("ADMIN")
                                .anyRequest()
                                .authenticated());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
