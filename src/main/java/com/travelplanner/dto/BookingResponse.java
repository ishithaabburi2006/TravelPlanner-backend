package com.travelplanner.dto;

import com.travelplanner.entity.Booking;

import java.time.LocalDate;

public record BookingResponse(
    Long id,
    String serviceType,
    String provider,
    String bookingReference,
    String details,
    LocalDate bookingDate,
    double amount,
    int travelers,
    String status,
    Long itineraryId,
    String itineraryTitle,
    Long userId,
    String userName,
    String userEmail
) {
  public static BookingResponse from(Booking booking) {
    return new BookingResponse(
        booking.getId(), booking.getServiceType(), booking.getProvider(),
        booking.getBookingReference(), booking.getDetails(), booking.getBookingDate(),
        booking.getAmount(), booking.getTravelers() == null ? 1 : booking.getTravelers(), booking.getStatus(),
        booking.getItinerary() == null ? null : booking.getItinerary().getId(),
        booking.getItinerary() == null ? null : booking.getItinerary().getTitle(),
        booking.getUser().getId(), booking.getUser().getName(), booking.getUser().getEmail()
    );
  }
}
