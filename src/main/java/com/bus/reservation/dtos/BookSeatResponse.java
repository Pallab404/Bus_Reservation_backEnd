package com.bus.reservation.dtos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSeatResponse {

    private Long bookingId;
    private String bookingStatus;
    private List<String> bookedSeats;
    private double totalAmount;
    private String paymentStatus;
    private LocalDateTime bookingTime;
    private String message;
}
