package com.bus.reservation.service.impl;

import com.bus.reservation.dtos.RouteRequest;
import com.bus.reservation.exception.ResourceAlreadyExistsException;
import com.bus.reservation.models.Route;
import com.bus.reservation.repository.RouteRepository;
import com.bus.reservation.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    @Override
    public Route addRoute(RouteRequest request) {
        String source = request.getSource().trim().toLowerCase();
        String destination = request.getDestination().trim().toLowerCase();

        if (routeRepository.existsBySourceIgnoreCaseAndDestinationIgnoreCase(source, destination)) {
            throw new ResourceAlreadyExistsException("Route already exists: " + source + " -> " + destination);
        }

        Route route = Route.builder()
                .source(source)
                .destination(destination)
                .distanceKm(request.getDistanceKm())
                .estimatedDurationMins(request.getEstimatedDurationMins())
                .build();

        Route savedRoute = routeRepository.save(route);

        if (!routeRepository.existsBySourceIgnoreCaseAndDestinationIgnoreCase(destination, source)) {
            Route reverseRoute = Route.builder()
                    .source(destination)
                    .destination(source)
                    .distanceKm(request.getDistanceKm())
                    .estimatedDurationMins(request.getEstimatedDurationMins())
                    .build();
            routeRepository.save(reverseRoute);
        }
        return savedRoute;
    }

}
