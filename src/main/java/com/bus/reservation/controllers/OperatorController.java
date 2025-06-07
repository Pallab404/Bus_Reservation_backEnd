package com.bus.reservation.controllers;

import com.bus.reservation.dtos.ScheduleBookingInfoResponse;
import com.bus.reservation.service.OperatorService;
import com.bus.reservation.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operator")
@RequiredArgsConstructor
public class OperatorController {
    private final OperatorService operatorService;

    @PreAuthorize("hasRole('OPERATOR')")
    @GetMapping("/schedule/{scheduleId}/bookings")
    public ResponseEntity<List<ScheduleBookingInfoResponse>> getScheduleBookings(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<ScheduleBookingInfoResponse> response = operatorService.getBookingsForSchedule(scheduleId, userDetails.getEmail());
        return ResponseEntity.ok(response);
    }
}
