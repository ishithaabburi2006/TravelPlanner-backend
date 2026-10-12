package com.travelplanner.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
  public record ApiError(Instant timestamp, int status, String error, String message, String path) {}

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> invalidBody(
      MethodArgumentNotValidException exception,
      HttpServletRequest request
  ) {
    String message = exception.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(error -> error.getField() + " " + error.getDefaultMessage())
        .orElse("Request validation failed");
    return error(HttpStatus.BAD_REQUEST, message, request);
  }

  @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
  public ResponseEntity<ApiError> invalidInput(Exception exception, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> unreadableBody(HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", request);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> responseStatus(
      ResponseStatusException exception,
      HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
    return error(status, exception.getReason() == null ? status.getReasonPhrase() : exception.getReason(), request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> conflict(HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, "The request conflicts with existing data", request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> unexpected(HttpServletRequest request) {
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
  }

  private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request) {
    ApiError body = new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI()
    );
    return ResponseEntity.status(status).body(body);
  }
}
