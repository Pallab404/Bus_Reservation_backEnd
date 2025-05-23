package com.bus.reservation.service;

import com.bus.reservation.dtos.BusSearchRequest;
import com.bus.reservation.dtos.BusSearchResponse;

import java.util.List;

public interface UserService {
    List<BusSearchResponse> searchScheduledBuses(BusSearchRequest request, String userEmail);
}
