package com.bus.reservation.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddBusRequest {

    @NotBlank(message = "Bus name is required")
    private String busName;

    @NotBlank(message = "Bus number is required")
    private String busNumber;

    @NotBlank(message = "Bus type is required")
    private String busType;

    @Min(value = 10, message = "bus must have at least 1 seat")
    @Max(value = 50, message = "Bus can't have more than 50 seats")
    private int totalSeats;
}
