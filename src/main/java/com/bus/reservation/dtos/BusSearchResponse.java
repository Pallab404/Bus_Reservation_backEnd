package com.bus.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusSearchResponse {

    private Long scheduleId;
    private Long busId;
    private String busName;
    private String busType;
    private String busNumber;
    private int totalSeats;
    private int availableSeats;
    private double fare;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String operatorName;
    private Long routeId;
}
