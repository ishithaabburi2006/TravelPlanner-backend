package com.travelplanner.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.travelplanner.repository.UserRepository;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final UserRepository users;

  public JwtAuthFilter(JwtService jwt, UserRepository users) {
    this.jwt = jwt;
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain
  ) throws ServletException, IOException {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      String token = authorization.substring(7).trim();
      if (jwt.valid(token)) {
        String email = jwt.extractEmail(token);
        users.findByEmailIgnoreCase(email).ifPresent(account -> {
          var authentication = new UsernamePasswordAuthenticationToken(
              account.getEmail(),
              null,
              List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
          );
          SecurityContextHolder.getContext().setAuthentication(authentication);
        });
      }
    }
    chain.doFilter(request, response);
  }
}
