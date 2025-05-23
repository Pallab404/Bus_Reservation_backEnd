package com.bus.reservation.controllers;

import com.bus.reservation.dtos.RouteRequest;
import com.bus.reservation.models.Route;
import com.bus.reservation.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/operator/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping("/add")
    public ResponseEntity<?> addRoute(@Valid @RequestBody RouteRequest request) {
        try {
            Route route = routeService.addRoute(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(route);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex.getMessage());
        }
    }
}
