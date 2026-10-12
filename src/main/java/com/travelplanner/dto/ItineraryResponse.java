package com.travelplanner.dto;

import com.travelplanner.entity.Itinerary;

import java.time.LocalDate;

public record ItineraryResponse(
    Long id,
    String title,
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    double budget,
    int travelers,
    String travelMode,
    String accommodation,
    String foodPreference,
    String activities,
    String dayPlan,
    String notes,
    String status,
    Long userId,
    String userName,
    String userEmail
) {
  public static ItineraryResponse from(Itinerary itinerary) {
    return new ItineraryResponse(
        itinerary.getId(), itinerary.getTitle(), itinerary.getDestination(),
        itinerary.getStartDate(), itinerary.getEndDate(), itinerary.getBudget(),
        itinerary.getTravelers(), itinerary.getTravelMode(), itinerary.getAccommodation(),
        itinerary.getFoodPreference(), itinerary.getActivities(), itinerary.getDayPlan(),
        itinerary.getNotes(), itinerary.getStatus(), itinerary.getUser().getId(),
        itinerary.getUser().getName(), itinerary.getUser().getEmail()
    );
  }
}
