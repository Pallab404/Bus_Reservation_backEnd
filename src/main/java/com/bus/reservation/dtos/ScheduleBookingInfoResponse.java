package com.bus.reservation.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleBookingInfoResponse {
    private Long bookingId;
    private String customerName;
    private String customerEmail;
    private String bookingStatus;
    private String paymentStatus;
    private int seatCount;
    private double totalAmount;
    private LocalDateTime bookingTime;
    private List<PassengerInfo> passengers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerInfo {
        private String seatNumber;
        private String name;
        private int age;
        private String gender;
    }
}
