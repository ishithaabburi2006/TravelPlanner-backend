package com.travelplanner.controller;

import com.travelplanner.dto.ItineraryRequest;
import com.travelplanner.entity.Itinerary;
import com.travelplanner.entity.Role;
import com.travelplanner.entity.User;
import com.travelplanner.repository.BookingRepository;
import com.travelplanner.repository.ItineraryRepository;
import com.travelplanner.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItineraryControllerTest {
  private final ItineraryRepository itineraries = mock(ItineraryRepository.class);
  private final UserRepository users = mock(UserRepository.class);
  private final BookingRepository bookings = mock(BookingRepository.class);
  private final ItineraryController controller = new ItineraryController(
      itineraries, users, bookings
  );

  @Test
  void ownerCanUpdateTripBudgetAndTripRequiresAdminReviewEvenWhenBudgetIsHigh() {
    User owner = user(7L, "owner@example.test");
    Itinerary trip = trip(13L, owner);
    when(itineraries.findById(13L)).thenReturn(Optional.of(trip));
    when(users.findByEmailIgnoreCase(owner.getEmail())).thenReturn(Optional.of(owner));
    when(itineraries.save(any(Itinerary.class))).thenAnswer(call -> call.getArgument(0));

    var result = controller.update(13L, request(25000), auth(owner.getEmail()));

    assertEquals(25000, result.budget());
    assertEquals("PENDING", result.status());
    verify(itineraries).save(trip);
  }

  @Test
  void ownerCanCreateTripAndEveryBudgetRequiresAdminReview() {
    User owner = user(7L, "owner@example.test");
    when(users.findByEmailIgnoreCase(owner.getEmail())).thenReturn(Optional.of(owner));
    when(itineraries.save(any(Itinerary.class))).thenAnswer(call -> {
      Itinerary saved = call.getArgument(0);
      saved.setId(21L);
      return saved;
    });

    var result = controller.create(request(250000), auth(owner.getEmail()));

    assertEquals("PENDING", result.getBody().status());
    assertEquals(250000, result.getBody().budget());
    verify(itineraries).save(any(Itinerary.class));
  }

  @Test
  void anotherUserCannotUpdateTripOrBudget() {
    User owner = user(7L, "owner@example.test");
    User attacker = user(8L, "other@example.test");
    Itinerary trip = trip(13L, owner);
    when(itineraries.findById(13L)).thenReturn(Optional.of(trip));
    when(users.findByEmailIgnoreCase(attacker.getEmail())).thenReturn(Optional.of(attacker));

    ResponseStatusException error = assertThrows(ResponseStatusException.class,
        () -> controller.update(13L, request(1), auth(attacker.getEmail())));

    assertEquals(403, error.getStatusCode().value());
    verify(itineraries, never()).save(any());
  }

  @Test
  void missingTripReturnsNotFound() {
    when(itineraries.findById(404L)).thenReturn(Optional.empty());

    ResponseStatusException error = assertThrows(ResponseStatusException.class,
        () -> controller.update(404L, request(1), auth("member@example.test")));

    assertEquals(404, error.getStatusCode().value());
    verify(users, never()).findByEmailIgnoreCase(any());
  }

  private static ItineraryRequest request(double budget) {
    return new ItineraryRequest("Trip", "Hyderabad", LocalDate.of(2026, 12, 1),
        LocalDate.of(2026, 12, 3), budget, 2, "Train", "Hotel", "Local cuisine",
        "Food tour", "Day 1: arrive", "Notes");
  }

  private static User user(Long id, String email) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    user.setName("Test User");
    user.setRole(Role.USER);
    return user;
  }

  private static Itinerary trip(Long id, User owner) {
    Itinerary trip = new Itinerary();
    trip.setId(id);
    trip.setUser(owner);
    trip.setTitle("Trip");
    trip.setDestination("Hyderabad");
    trip.setStartDate(LocalDate.of(2026, 12, 1));
    trip.setEndDate(LocalDate.of(2026, 12, 3));
    trip.setBudget(15000);
    trip.setTravelers(2);
    trip.setTravelMode("Train");
    trip.setAccommodation("Hotel");
    return trip;
  }

  private static UsernamePasswordAuthenticationToken auth(String email) {
    return UsernamePasswordAuthenticationToken.authenticated(email, null, List.of());
  }
}
