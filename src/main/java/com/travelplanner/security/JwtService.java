package com.travelplanner.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

@Service
public class JwtService {
  private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN");

  private final SecretKey key;
  private final long expiration;
  private final String issuer;

  public JwtService(
      @Value("${app.jwt.secret}") String encodedSecret,
      @Value("${app.jwt.expiration}") long expiration,
      @Value("${app.jwt.issuer}") String issuer
  ) {
    byte[] secret = Decoders.BASE64.decode(encodedSecret);
    if (secret.length < 32) {
      throw new IllegalArgumentException("JWT_SECRET must decode to at least 32 bytes");
    }
    if (expiration <= 0) {
      throw new IllegalArgumentException("JWT_EXPIRATION must be positive");
    }
    this.key = Keys.hmacShaKeyFor(secret);
    this.expiration = expiration;
    this.issuer = issuer;
  }

  public String generate(String email, String role) {
    if (email == null || email.isBlank() || !ALLOWED_ROLES.contains(role)) {
      throw new IllegalArgumentException("JWT subject and role are invalid");
    }

    Date now = new Date();
    return Jwts.builder()
        .issuer(issuer)
        .subject(email)
        .claim("role", role)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expiration))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .requireIssuer(issuer)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String extractEmail(String token) {
    return parse(token).getSubject();
  }

  public String extractRole(String token) {
    return parse(token).get("role", String.class);
  }

  public boolean valid(String token) {
    try {
      Claims claims = parse(token);
      String role = claims.get("role", String.class);
      return claims.getSubject() != null
          && !claims.getSubject().isBlank()
          && role != null
          && ALLOWED_ROLES.contains(role);
    } catch (JwtException | IllegalArgumentException exception) {
      return false;
    }
  }
}
