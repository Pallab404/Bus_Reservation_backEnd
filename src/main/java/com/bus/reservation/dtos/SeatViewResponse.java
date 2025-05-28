package com.bus.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatViewResponse {
    private Long seatId;
    private String seatNumber;
    private String seatType;
    private boolean isBooked;
    private double fare;
}
