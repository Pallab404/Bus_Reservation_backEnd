package com.bus.reservation.controllers;

import com.bus.reservation.dtos.BusSearchRequest;
import com.bus.reservation.dtos.BusSearchResponse;
import com.bus.reservation.service.UserDetailsImpl;
import com.bus.reservation.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
