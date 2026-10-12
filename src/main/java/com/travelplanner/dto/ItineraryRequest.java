package com.travelplanner.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record ItineraryRequest(
  @NotBlank @Size(max = 160) String title,
  @NotBlank @Size(max = 160) String destination,
  @NotNull LocalDate startDate,
  @NotNull LocalDate endDate,
  @PositiveOrZero double budget,
  @Min(1) @Max(30) int travelers,
  @NotBlank @Size(max = 80) String travelMode,
  @NotBlank @Size(max = 120) String accommodation,
  @Size(max = 120) String foodPreference,
  @Size(max = 2200) String activities,
  @Size(max = 3500) String dayPlan,
  @Size(max = 3000) String notes
) {}
