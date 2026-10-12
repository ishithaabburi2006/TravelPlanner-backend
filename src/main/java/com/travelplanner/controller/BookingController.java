package com.travelplanner.controller;

import com.travelplanner.dto.BookingRequest;
import com.travelplanner.dto.BookingResponse;
import com.travelplanner.entity.Booking;
import com.travelplanner.entity.Itinerary;
import com.travelplanner.entity.User;
import com.travelplanner.repository.BookingRepository;
import com.travelplanner.repository.ItineraryRepository;
import com.travelplanner.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
  private final BookingRepository repo;
  private final UserRepository users;
  private final ItineraryRepository itineraries;

  public BookingController(
      BookingRepository repo,
      UserRepository users,
      ItineraryRepository itineraries
  ) {
    this.repo = repo;
    this.users = users;
    this.itineraries = itineraries;
  }

  private User currentUser(Authentication authentication) {
    return users.findByEmailIgnoreCase(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  @GetMapping
  public List<BookingResponse> mine(Authentication authentication) {
    User user = currentUser(authentication);
    return repo.findByUserIdOrderByBookingDateDesc(user.getId()).stream()
        .map(BookingResponse::from)
        .toList();
  }

  @PostMapping
  public ResponseEntity<BookingResponse> create(
      @Valid @RequestBody BookingRequest request,
      Authentication authentication
  ) {
    User user = currentUser(authentication);
    Booking booking = new Booking();
    booking.setServiceType(request.serviceType());
    booking.setProvider(request.provider().trim());
    booking.setBookingReference(request.bookingReference());
    booking.setDetails(request.details());
    booking.setBookingDate(request.bookingDate());
    booking.setAmount(request.amount());
    booking.setTravelers(request.travelers());
    booking.setUser(user);

    if (request.itineraryId() != null) {
      Itinerary itinerary = itineraries.findById(request.itineraryId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
      if (!itinerary.getUser().getId().equals(user.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot link a booking to another user's trip");
      }
      booking.setItinerary(itinerary);
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BookingResponse.from(repo.save(booking)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancel(
      @PathVariable Long id,
      Authentication authentication
  ) {
    User user = currentUser(authentication);
    Booking booking = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    if (!booking.getUser().getId().equals(user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot cancel another user's booking");
    }
    if ("CANCELLED".equals(booking.getStatus())) {
      return ResponseEntity.noContent().build();
    }
    if ("COMPLETED".equals(booking.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "A completed booking cannot be cancelled");
    }

    booking.setStatus("CANCELLED");
    repo.save(booking);
    return ResponseEntity.noContent().build();
  }
}
