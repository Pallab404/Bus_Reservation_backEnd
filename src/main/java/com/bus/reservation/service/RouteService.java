package com.bus.reservation.service;

import com.bus.reservation.dtos.RouteRequest;
import com.bus.reservation.models.Route;

import java.util.List;

public interface RouteService {

    Route addRoute(RouteRequest request);
    List<Route> getAllRoutes();
    List<String> getAllUniqueSources();
    List<String> getAllUniqueDestinations();

}
