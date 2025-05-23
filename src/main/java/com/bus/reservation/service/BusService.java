package com.bus.reservation.service;

import com.bus.reservation.dtos.AddBusRequest;
import com.bus.reservation.dtos.BusSummaryResponse;

import java.util.List;


public interface BusService {
    void addBus(AddBusRequest request, String operatorEmail);
    List<BusSummaryResponse> getBusesForOperator(String operatorEmail);
}
