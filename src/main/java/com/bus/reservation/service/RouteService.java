package com.bus.reservation.service;

import com.bus.reservation.dtos.RouteRequest;
import com.bus.reservation.models.Route;

public interface RouteService {

    Route addRoute(RouteRequest request);
}
