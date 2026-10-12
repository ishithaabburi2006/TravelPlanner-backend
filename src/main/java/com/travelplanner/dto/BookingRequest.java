package com.travelplanner.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookingRequest(
    @NotBlank @Pattern(regexp = "HOTEL|FLIGHT|TRAIN|BUS|ACTIVITY") String serviceType,
    @NotBlank @Size(max = 180) String provider,
    @Size(max = 100) String bookingReference,
    @Size(max = 1500) String details,
    @NotNull LocalDate bookingDate,
    @PositiveOrZero double amount,
    @Min(1) @Max(30) int travelers,
    Long itineraryId
) {}
