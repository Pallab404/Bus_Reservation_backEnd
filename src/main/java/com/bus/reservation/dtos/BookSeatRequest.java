package com.bus.reservation.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSeatRequest {

    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(max = 6, message = "Cannot book more than 6 seats")
    private List<@Pattern(regexp = "^[A-Z0-9]+$", message = "Seat number format is invalid") String> seatNumbers;

    @NotEmpty(message = "Passenger details are required")
    @Size(max = 6, message = "Cannot have more than 6 passengers")
    private List<@Valid PassengerRequest> passengers;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
