package com.bus.reservation.controllers;

import com.bus.reservation.dtos.*;
import com.bus.reservation.service.UserDetailsImpl;
import com.bus.reservation.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/search-buses")
    public ResponseEntity<List<BusSearchResponse>> searchBuses(@Valid @RequestBody BusSearchRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<BusSearchResponse> results = userService.searchScheduledBuses(request,  userDetails.getEmail());
        return ResponseEntity.ok(results);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/seat-layout")
    public ResponseEntity<List<SeatViewResponse>> getSeatLayout(@RequestParam Long scheduleId) {
        List<SeatViewResponse> seatLayout = userService.getSeatLayout(scheduleId);
        return ResponseEntity.ok(seatLayout);
    }

    @PostMapping("/book-seat")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookSeatResponse> bookSeat(
            @RequestBody @Valid BookSeatRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        BookSeatResponse response = userService.bookSeat(request, userDetails.getEmail());
        return ResponseEntity.ok(response);
    }
}
