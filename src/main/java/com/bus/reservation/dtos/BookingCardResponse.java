package com.bus.reservation.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class BookingCardResponse {
    private Long bookingId;
    private String busName;
    private String busNumber;
    private String source;
    private String destination;
    private LocalDate journeyDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String bookingStatus; // CONFIRMED / CANCELLED / COMPLETED
    private boolean isCancelable;
    private List<PassengerInfo> passengers;
}

