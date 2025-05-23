package com.bus.reservation.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ScheduleSummaryResponse {
    private Long scheduleId;
    private LocalDate journeyDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String source;
    private String destination;
    private Double fare;
    private Integer availableSeats;
    private String status;
}
