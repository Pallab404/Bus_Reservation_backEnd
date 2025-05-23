package com.bus.reservation.controllers;

import com.bus.reservation.dtos.AddBusRequest;
import com.bus.reservation.dtos.BusSummaryResponse;
import com.bus.reservation.models.User;
import com.bus.reservation.service.BusService;
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
public class BusController {
    private final BusService busService;

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping("/addBus")
    public ResponseEntity<String> addBus(@Valid @RequestBody AddBusRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        busService.addBus(request, userDetails.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body("Bus added successfully");
    }

    @PreAuthorize("hasRole('OPERATOR')")
    @GetMapping("/my-buses")
    public ResponseEntity<List<BusSummaryResponse>> getOperatorBuses(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<BusSummaryResponse> buses = busService.getBusesForOperator(userDetails.getEmail());
        return ResponseEntity.ok(buses);
    }
}
