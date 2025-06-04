package com.bus.reservation.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PassengerInfo {
    private String seatNumber;
    private String name;
    private int age;
    private String gender;
}

