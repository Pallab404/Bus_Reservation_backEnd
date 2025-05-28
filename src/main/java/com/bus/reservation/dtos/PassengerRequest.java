package com.bus.reservation.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerRequest {

    @NotBlank(message = "Passenger name is required")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Source must contain only letters and spaces")
    private String name;

    @Min(value = 1, message = "Passenger age must be at least 1")
    @Max(value = 120, message = "Passenger age must be reasonable")
    private int age;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;
}

