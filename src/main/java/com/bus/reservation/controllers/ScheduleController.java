package com.bus.reservation.controllers;

import com.bus.reservation.dtos.CreateScheduleRequest;
import com.bus.reservation.dtos.ScheduleSummaryResponse;
import com.bus.reservation.service.ScheduleService;
import com.bus.reservation.service.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operator")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping("/create-schedule")
    public ResponseEntity<String> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        scheduleService.createSchedule(request, userDetails.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body("Schedules created successfully for 30 days with return trips.");
    }

    @PreAuthorize("hasRole('OPERATOR')")
    @GetMapping("/schedules/{busId}")
    public ResponseEntity<List<ScheduleSummaryResponse>> getSchedulesForBus(
            @PathVariable Long busId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ScheduleSummaryResponse> schedules = scheduleService.getSchedulesForBus(busId, userDetails.getEmail());
        return ResponseEntity.ok(schedules);
    }

    @PreAuthorize("hasRole('OPERATOR')")
    @DeleteMapping("/cancel/{scheduleId}")
    public ResponseEntity<String> cancelSchedule(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        scheduleService.cancelSchedule(scheduleId, userDetails.getEmail());
        return ResponseEntity.ok("Schedule cancelled successfully.");
    }

}
