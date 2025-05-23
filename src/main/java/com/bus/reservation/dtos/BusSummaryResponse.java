package com.bus.reservation.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusSummaryResponse {
    private Long id;
    private String busNumber;
    private String busType;
    private int totalSeats;
    private boolean scheduled; // true if schedule exists
}
