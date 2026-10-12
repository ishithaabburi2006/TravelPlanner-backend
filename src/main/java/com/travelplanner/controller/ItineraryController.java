package com.travelplanner.controller;

import com.travelplanner.dto.ItineraryRequest;
import com.travelplanner.dto.ItineraryResponse;
import com.travelplanner.entity.Itinerary;
import com.travelplanner.entity.User;
import com.travelplanner.repository.ItineraryRepository;
import com.travelplanner.repository.UserRepository;
import com.travelplanner.repository.BookingRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {
  private final ItineraryRepository repo;
  private final UserRepository users;
  private final BookingRepository bookings;

  public ItineraryController(
      ItineraryRepository repo,
      UserRepository users,
      BookingRepository bookings
  ) {
    this.repo = repo;
    this.users = users;
    this.bookings = bookings;
  }

  private User user(Authentication authentication) {
    return users.findByEmailIgnoreCase(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private void fill(Itinerary itinerary, ItineraryRequest request) {
    if (request.endDate().isBefore(request.startDate())) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }

    itinerary.setTitle(request.title());
    itinerary.setDestination(request.destination().trim());
    itinerary.setStartDate(request.startDate());
    itinerary.setEndDate(request.endDate());
    itinerary.setBudget(request.budget());
    itinerary.setTravelers(request.travelers());
    itinerary.setTravelMode(request.travelMode());
    itinerary.setAccommodation(request.accommodation());
    itinerary.setFoodPreference(request.foodPreference());
    itinerary.setActivities(request.activities());
    itinerary.setDayPlan(request.dayPlan());
    itinerary.setNotes(request.notes());
  }

  private void updateApprovalStatus(Itinerary itinerary) {
    // Every user submission must be reviewed, irrespective of the trip budget.
    itinerary.setStatus("PENDING");
  }

  @GetMapping
  public List<ItineraryResponse> mine(Authentication authentication) {
    User currentUser = user(authentication);
    return repo.findByUserIdOrderByStartDateDesc(currentUser.getId()).stream()
        .map(ItineraryResponse::from)
        .toList();
  }

  @PostMapping
  public ResponseEntity<ItineraryResponse> create(
      @Valid @RequestBody ItineraryRequest request,
      Authentication authentication
  ) {
    Itinerary itinerary = new Itinerary();
    fill(itinerary, request);
    itinerary.setUser(user(authentication));
    updateApprovalStatus(itinerary);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ItineraryResponse.from(repo.save(itinerary)));
  }

  @PutMapping("/{id}")
  public ItineraryResponse update(
      @PathVariable Long id,
      @Valid @RequestBody ItineraryRequest request,
      Authentication authentication
  ) {
    Itinerary itinerary = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Itinerary not found"));
    User currentUser = user(authentication);

    if (!itinerary.getUser().getId().equals(currentUser.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update another user's itinerary");
    }

    fill(itinerary, request);
    updateApprovalStatus(itinerary);

    return ItineraryResponse.from(repo.save(itinerary));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id, Authentication authentication) {
    Itinerary itinerary = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Itinerary not found"));
    User currentUser = user(authentication);

    if (!itinerary.getUser().getId().equals(currentUser.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete another user's itinerary");
    }

    if (bookings.countByItineraryId(itinerary.getId()) > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Remove or unlink this trip's bookings before deleting it");
    }

    repo.delete(itinerary);
  }

  @GetMapping("/{id}/summary")
  public Map<String, Object> summary(
      @PathVariable Long id,
      Authentication authentication
  ) {
    Itinerary itinerary = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Itinerary not found"));
    User currentUser = user(authentication);

    if (!itinerary.getUser().getId().equals(currentUser.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view another user's itinerary");
    }

    long days = ChronoUnit.DAYS.between(
        itinerary.getStartDate(),
        itinerary.getEndDate()
    ) + 1;

    return Map.of(
        "itinerary", ItineraryResponse.from(itinerary),
        "days", days,
        "dailyBudget",
        days > 0 ? itinerary.getBudget() / days : itinerary.getBudget()
    );
  }
}
