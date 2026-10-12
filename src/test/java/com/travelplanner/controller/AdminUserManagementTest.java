package com.travelplanner.controller;

import com.travelplanner.dto.AdminCreateUserRequest;
import com.travelplanner.dto.AdminUpdateUserRequest;
import com.travelplanner.entity.Role;
import com.travelplanner.entity.User;
import com.travelplanner.repository.AttractionRepository;
import com.travelplanner.repository.BookingRepository;
import com.travelplanner.repository.DestinationRepository;
import com.travelplanner.repository.ItineraryRepository;
import com.travelplanner.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminUserManagementTest {
  private final UserRepository users = mock(UserRepository.class);
  private final ItineraryRepository itineraries = mock(ItineraryRepository.class);
  private final BookingRepository bookings = mock(BookingRepository.class);
  private final AdminController controller = new AdminController(
      users,
      mock(DestinationRepository.class),
      itineraries,
      bookings,
      mock(AttractionRepository.class),
      new BCryptPasswordEncoder()
  );

  @Test
  void createUserNormalizesEmailAndHashesPassword() {
    when(users.findByEmailIgnoreCase("new.user@example.com")).thenReturn(Optional.empty());
    when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    controller.createUser(new AdminCreateUserRequest(
        "New User", " New.User@Example.com ", "a-long-unique-password", Role.USER
    ));

    var saved = org.mockito.ArgumentCaptor.forClass(User.class);
    verify(users).save(saved.capture());
    assertEquals("new.user@example.com", saved.getValue().getEmail());
    assertNotEquals("a-long-unique-password", saved.getValue().getPassword());
    assertTrue(new BCryptPasswordEncoder().matches("a-long-unique-password", saved.getValue().getPassword()));
  }

  @Test
  void cannotDeleteTheLastAdmin() {
    User target = user(2L, "other-admin@example.com", Role.ADMIN);
    User actor = user(1L, "signed-in-admin@example.com", Role.ADMIN);
    when(users.findById(2L)).thenReturn(Optional.of(target));
    when(users.findByEmailIgnoreCase("signed-in-admin@example.com")).thenReturn(Optional.of(actor));
    when(users.countByRole(Role.ADMIN)).thenReturn(1L);

    ResponseStatusException error = assertThrows(ResponseStatusException.class,
        () -> controller.deleteUser(2L, UsernamePasswordAuthenticationToken.authenticated(
            "signed-in-admin@example.com", null, java.util.List.of()
        )));

    assertEquals(409, error.getStatusCode().value());
    verify(users, never()).delete(any(User.class));
  }

  @Test
  void cannotDemoteTheSignedInAdmin() {
    User actor = user(1L, "signed-in-admin@example.com", Role.ADMIN);
    when(users.findById(1L)).thenReturn(Optional.of(actor));
    when(users.findByEmailIgnoreCase("signed-in-admin@example.com")).thenReturn(Optional.of(actor));

    ResponseStatusException error = assertThrows(ResponseStatusException.class,
        () -> controller.updateUser(1L,
            new AdminUpdateUserRequest("Admin", "signed-in-admin@example.com", Role.USER, null),
            UsernamePasswordAuthenticationToken.authenticated("signed-in-admin@example.com", null, java.util.List.of())));

    assertEquals(409, error.getStatusCode().value());
    verify(users, never()).save(any(User.class));
  }

  private static User user(Long id, String email, Role role) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    user.setName("Test Admin");
    user.setRole(role);
    return user;
  }
}
