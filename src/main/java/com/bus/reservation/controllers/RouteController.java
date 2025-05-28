package com.bus.reservation.controllers;

import com.bus.reservation.dtos.RouteRequest;
import com.bus.reservation.models.Route;
import com.bus.reservation.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;


    @PostMapping("/add")
    public ResponseEntity<?> addRoute(@Valid @RequestBody RouteRequest request) {
        try {
            Route route = routeService.addRoute(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(route);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex.getMessage());
        }
    }

    @GetMapping("/get-routes")
    public ResponseEntity<List<Route>> getAllRoutes() {
        List<Route> routes = routeService.getAllRoutes();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/sources")
    public ResponseEntity<List<String>> getAllSources() {
        return ResponseEntity.ok(routeService.getAllUniqueSources());
    }

    @GetMapping("/destinations")
    public ResponseEntity<List<String>> getAllDestinations() {
        return ResponseEntity.ok(routeService.getAllUniqueDestinations());
    }
}
