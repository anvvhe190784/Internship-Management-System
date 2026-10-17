package com.example.internship.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  private boolean success;

  @JsonProperty("status_code")
  private Integer statusCode;

  @JsonProperty("error_code")
  private String errorCode;

  private String message;
  private T data;
  private List<FieldErrorDetail> errors;

  @Builder.Default
  private String timestamp = Instant.now().toString();

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FieldErrorDetail {
    private String field;
    private String message;
  }

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .statusCode(200)
        .message("Success")
        .data(data)
        .timestamp(Instant.now().toString())
        .build();
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .statusCode(200)
        .message(message)
        .data(data)
        .timestamp(Instant.now().toString())
        .build();
  }

  public static <T> ApiResponse<T> success(int statusCode, String message, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .statusCode(statusCode)
        .message(message)
        .data(data)
        .timestamp(Instant.now().toString())
        .build();
  }

  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder()
        .success(false)
        .statusCode(400)
        .errorCode("INVALID_INPUT_DATA")
        .message(message)
        .timestamp(Instant.now().toString())
        .build();
  }

  public static <T> ApiResponse<T> error(int statusCode, String errorCode, String message) {
    return ApiResponse.<T>builder()
        .success(false)
        .statusCode(statusCode)
        .errorCode(errorCode)
        .message(message)
        .timestamp(Instant.now().toString())
        .build();
  }

  public static <T> ApiResponse<T> error(
      int statusCode, String errorCode, String message, List<FieldErrorDetail> errors) {
    return ApiResponse.<T>builder()
        .success(false)
        .statusCode(statusCode)
        .errorCode(errorCode)
        .message(message)
        .errors(errors)
        .timestamp(Instant.now().toString())
        .build();
  }
}
