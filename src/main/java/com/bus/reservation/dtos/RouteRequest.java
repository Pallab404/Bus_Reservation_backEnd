package com.bus.reservation.dtos;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteRequest {

    @NotBlank(message = "Source is required")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Source must contain only letters and spaces")
    private String source;

    @NotBlank(message = "Destination is required")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Destination must contain only letters and spaces")
    private String destination;

    @NotNull(message = "Distance is required")
    @DecimalMin(value = "40.0", message = "Distance must be at least 40 km")
    private Double distanceKm;

    @NotNull(message = "Estimated duration is required")
    @Min(value = 60, message = "Estimated duration must be at least 60 mins")
    private Integer estimatedDurationMins;
}
