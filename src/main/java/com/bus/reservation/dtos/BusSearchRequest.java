package com.bus.reservation.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BusSearchRequest {

    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "destination is required")
    private String destination;

    @NotNull(message = "Journey date is required")
    @FutureOrPresent(message = "Journey date must be today or in the future")
    private LocalDate journeyDate;

}
