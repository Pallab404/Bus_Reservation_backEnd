package com.bus.reservation.service.impl;

import com.bus.reservation.dtos.BusSearchRequest;
import com.bus.reservation.dtos.BusSearchResponse;
import com.bus.reservation.exception.ResourceNotFoundException;
import com.bus.reservation.models.Bus;
import com.bus.reservation.models.Route;
import com.bus.reservation.models.Schedule;
import com.bus.reservation.models.User;
import com.bus.reservation.repository.RouteRepository;
import com.bus.reservation.repository.ScheduleRepository;
import com.bus.reservation.repository.UserRepository;
import com.bus.reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Override
    public List<BusSearchResponse> searchScheduledBuses(BusSearchRequest request, String userEmail) {
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Route route = routeRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(
                        request.getSource(), request.getDestination())
                .orElseThrow(() -> new ResourceNotFoundException("No route found for given source and destination"));

        List<Schedule> schedules = scheduleRepository.findByRouteIdAndJourneyDateAndIsActiveTrue(
                route.getId(), request.getJourneyDate());

        return schedules.stream().map(schedule -> {
            Bus bus = schedule.getBus();
            User operator = bus.getOperator();
            return BusSearchResponse.builder()
                    .scheduleId(schedule.getId())
                    .busId(bus.getId())
                    .busName(bus.getBusName())
                    .busType(bus.getBusType())
                    .busNumber(bus.getBusNumber())
                    .totalSeats(bus.getTotalSeats())
                    .availableSeats(schedule.getAvailableSeats())
                    .fare(schedule.getFare())
                    .departureTime(schedule.getDepartureTime())
                    .arrivalTime(schedule.getArrivalTime())
                    .operatorName(operator.getCompanyName())
                    .routeId(route.getId())
                    .build();
        }).collect(Collectors.toList());
    }
}
