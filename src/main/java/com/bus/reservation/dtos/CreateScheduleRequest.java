package com.bus.reservation.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class CreateScheduleRequest {
    @NotNull(message = "Bus ID is required")
    private Long busId;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    @NotNull(message = "Journey start date is required")
    @FutureOrPresent(message = "Journey start date must be today or in the future")
    private LocalDate journeyStartDate;

    @NotNull(message = "Departure time is required")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalTime arrivalTime;

    @NotNull(message = "Fare is required")
    @Positive(message = "Fare must be positive")
    private Double fare;

}
