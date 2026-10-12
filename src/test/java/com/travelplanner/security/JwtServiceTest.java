package com.travelplanner.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
  private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);
  private final JwtService service = new JwtService(SECRET, 60_000, "travel-planner-test");

  @Test
  void acceptsSignedTokenWithExpectedIssuerAndRole() {
    String token = service.generate("member@example.test", "USER");

    assertTrue(service.valid(token));
    assertEquals("member@example.test", service.extractEmail(token));
    assertEquals("USER", service.extractRole(token));
  }

  @Test
  void rejectsTokenSignedWithDifferentKey() {
    byte[] otherKey = new byte[32];
    otherKey[0] = 1;
    String tampered = Jwts.builder()
        .subject("member@example.test")
        .issuer("travel-planner-test")
        .claim("role", "ADMIN")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 60_000))
        .signWith(Keys.hmacShaKeyFor(otherKey))
        .compact();

    assertFalse(service.valid(tampered));
  }

  @Test
  void rejectsUnexpectedRoleWhenGeneratingToken() {
    assertThrows(IllegalArgumentException.class, () -> service.generate("member@example.test", "SUPERUSER"));
  }

  @Test
  void rejectsWeakOrNonPositiveConfiguration() {
    assertThrows(IllegalArgumentException.class, () -> new JwtService(Base64.getEncoder().encodeToString(new byte[16]), 60_000, "issuer"));
    assertThrows(IllegalArgumentException.class, () -> new JwtService(SECRET, 0, "issuer"));
  }
}
