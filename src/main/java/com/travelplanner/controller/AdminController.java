package com.travelplanner.controller;

import com.travelplanner.dto.AdminUserResponse;
import com.travelplanner.dto.AdminCreateUserRequest;
import com.travelplanner.dto.AdminUpdateUserRequest;
import com.travelplanner.dto.BookingResponse;
import com.travelplanner.dto.ItineraryResponse;
import com.travelplanner.entity.Booking;
import com.travelplanner.entity.Destination;
import com.travelplanner.entity.Itinerary;
import com.travelplanner.entity.Role;
import com.travelplanner.entity.User;
import com.travelplanner.repository.BookingRepository;
import com.travelplanner.repository.AttractionRepository;
import com.travelplanner.repository.DestinationRepository;
import com.travelplanner.repository.ItineraryRepository;
import com.travelplanner.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
  private final UserRepository users;
  private final DestinationRepository destinations;
  private final ItineraryRepository itineraries;
  private final BookingRepository bookings;
  private final AttractionRepository attractions;
  private final PasswordEncoder passwordEncoder;

  public AdminController(
      UserRepository users,
      DestinationRepository destinations,
      ItineraryRepository itineraries,
      BookingRepository bookings,
      AttractionRepository attractions,
      PasswordEncoder passwordEncoder
  ) {
    this.users = users;
    this.destinations = destinations;
    this.itineraries = itineraries;
    this.bookings = bookings;
    this.attractions = attractions;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/stats")
  public Map<String, Object> stats() {
    return Map.of(
        "users", users.count(),
        "destinations", destinations.count(),
        "itineraries", itineraries.count(),
        "bookings", bookings.count()
    );
  }

  @GetMapping("/users")
  public List<AdminUserResponse> users() {
    return users.findAll().stream()
        .map(user -> new AdminUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        ))
        .toList();
  }

  @PostMapping("/users")
  public AdminUserResponse createUser(@jakarta.validation.Valid @RequestBody AdminCreateUserRequest request) {
    String email = normalizeEmail(request.email());
    ensureEmailAvailable(email, null);
    ensureBcryptPasswordLength(request.password());

    User user = new User();
    user.setName(request.name().trim());
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRole(request.role());
    return toAdminUser(users.save(user));
  }

  @PutMapping("/users/{id}")
  @Transactional
  public AdminUserResponse updateUser(
      @PathVariable Long id,
      @jakarta.validation.Valid @RequestBody AdminUpdateUserRequest request,
      Authentication authentication
  ) {
    User target = users.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    User actor = users.findByEmailIgnoreCase(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin account not found"));
    String email = normalizeEmail(request.email());
    ensureEmailAvailable(email, id);

    if (actor.getId().equals(id) && !target.getEmail().equalsIgnoreCase(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Use a separate admin account to change this account's email");
    }
    if (actor.getId().equals(id) && request.role() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Use another admin account to change your admin role");
    }
    if (target.getRole() == Role.ADMIN && request.role() != Role.ADMIN
        && users.countByRole(Role.ADMIN) <= 1) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "The last admin account cannot be demoted");
    }
    if (request.password() != null && !request.password().isBlank()) {
      ensureBcryptPasswordLength(request.password());
      target.setPassword(passwordEncoder.encode(request.password()));
    }

    target.setName(request.name().trim());
    target.setEmail(email);
    target.setRole(request.role());
    return toAdminUser(users.save(target));
  }

  @DeleteMapping("/users/{id}")
  @Transactional
  public void deleteUser(@PathVariable Long id, Authentication authentication) {
    User target = users.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    User actor = users.findByEmailIgnoreCase(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin account not found"));
    if (actor.getId().equals(id)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot delete your signed-in admin account");
    }
    if (target.getRole() == Role.ADMIN && users.countByRole(Role.ADMIN) <= 1) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "The last admin account cannot be deleted");
    }
    if (itineraries.countByUserId(id) > 0 || bookings.countByUserId(id) > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This account owns trips or booking records; preserve those records before deleting it");
    }
    users.delete(target);
  }

  private void ensureEmailAvailable(String email, Long exceptId) {
    users.findByEmailIgnoreCase(email).ifPresent(existing -> {
      if (exceptId == null || !existing.getId().equals(exceptId)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email address is already in use");
      }
    });
  }

  private static void ensureBcryptPasswordLength(String password) {
    if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
    }
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private static AdminUserResponse toAdminUser(User user) {
    return new AdminUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
  }

  @GetMapping("/destinations")
  public List<Destination> destinations() {
    return destinations.findAll();
  }

  @GetMapping("/bookings")
  public List<BookingResponse> bookings() {
    return bookings.findAll().stream().map(BookingResponse::from).toList();
  }

  @GetMapping("/itineraries")
  public List<ItineraryResponse> itineraries() {
    return itineraries.findAll().stream().map(ItineraryResponse::from).toList();
  }

  @PostMapping("/destinations")
  public Destination addDestination(@RequestBody Destination destination) {
    destination.setId(null);
    return destinations.save(destination);
  }

  @PutMapping("/destinations/{id}")
  public Destination updateDestination(
      @PathVariable Long id,
      @RequestBody Destination destination
  ) {
    Destination existing = destinations.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination not found"));
    destination.setId(existing.getId());
    return destinations.save(destination);
  }

  @DeleteMapping("/destinations/{id}")
  @Transactional
  public void deleteDestination(@PathVariable Long id) {
    if (!destinations.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination not found");
    }
    attractions.deleteByDestinationId(id);
    destinations.deleteById(id);
  }

  @PatchMapping("/bookings/{id}/status")
  public BookingResponse updateBookingStatus(
      @PathVariable Long id,
      @RequestParam String status
  ) {
    String nextStatus = status.toUpperCase(Locale.ROOT);
    if (!Set.of("RECORDED", "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED").contains(nextStatus)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported booking status");
    }

    Booking booking = bookings.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    String current = booking.getStatus();
    boolean allowed = current.equals(nextStatus)
        || (("RECORDED".equals(current) || "PENDING".equals(current))
            && Set.of("CONFIRMED", "CANCELLED").contains(nextStatus))
        || ("CONFIRMED".equals(current) && Set.of("CANCELLED", "COMPLETED").contains(nextStatus));
    if (!allowed) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid booking status transition");
    }
    booking.setStatus(nextStatus);
    return BookingResponse.from(bookings.save(booking));
  }

  @PatchMapping("/itineraries/{id}/status")
  public ItineraryResponse updateItineraryStatus(
      @PathVariable Long id,
      @RequestParam String status
  ) {
    String normalizedStatus = status.toUpperCase(Locale.ROOT);

    if (!Set.of("APPROVED", "REJECTED").contains(normalizedStatus)) {
      throw new IllegalArgumentException(
          "Status must be APPROVED or REJECTED"
      );
    }

    Itinerary itinerary = itineraries.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip request not found"));
    if (!"PENDING".equals(itinerary.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending trip requests can be reviewed");
    }
    itinerary.setStatus(normalizedStatus);
    return ItineraryResponse.from(itineraries.save(itinerary));
  }

  @DeleteMapping("/itineraries/{id}")
  public void deleteItinerary(@PathVariable Long id) {
    Itinerary itinerary = itineraries.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Itinerary not found"));
    if (bookings.countByItineraryId(id) > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Remove or unlink this trip's bookings before deleting it");
    }
    itineraries.delete(itinerary);
  }
}
