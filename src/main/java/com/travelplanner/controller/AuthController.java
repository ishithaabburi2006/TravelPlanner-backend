package com.travelplanner.controller;

import com.travelplanner.dto.AuthRequest;
import com.travelplanner.dto.AuthResponse;
import com.travelplanner.dto.RegisterRequest;
import com.travelplanner.entity.Role;
import com.travelplanner.entity.User;
import com.travelplanner.repository.UserRepository;
import com.travelplanner.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
    this.users = users;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    String email = normalizeEmail(request.email());
    if (users.findByEmailIgnoreCase(email).isPresent()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Unable to register this email address");
    }
    if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
      return ResponseEntity.badRequest().body("Password must be at most 72 UTF-8 bytes");
    }

    User user = new User();
    user.setName(request.name().trim());
    user.setEmail(email);
    user.setPassword(encoder.encode(request.password()));
    user.setRole(Role.USER);
    try {
      users.save(user);
    } catch (DataIntegrityViolationException duplicate) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Unable to register this email address");
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body("Registration successful. Please login.");
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
    String email = normalizeEmail(request.email());
    User user = users.findByEmailIgnoreCase(email).orElse(null);
    if (user == null || !encoder.matches(request.password(), user.getPassword())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }

    String token = jwt.generate(user.getEmail(), user.getRole().name());
    return ResponseEntity.ok(new AuthResponse(
        token,
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole().name()
    ));
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
