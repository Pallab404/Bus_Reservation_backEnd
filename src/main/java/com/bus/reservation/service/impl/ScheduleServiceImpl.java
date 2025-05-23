package com.bus.reservation.service.impl;

import com.bus.reservation.dtos.CreateScheduleRequest;
import com.bus.reservation.dtos.ScheduleSummaryResponse;
import com.bus.reservation.exception.ResourceNotFoundException;
import com.bus.reservation.models.*;
import com.bus.reservation.repository.*;
import com.bus.reservation.service.ScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void createSchedule(CreateScheduleRequest request, String operatorEmail) {
        User operator = userRepository.findByEmail(operatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        if (!bus.getOperator().getId().equals(operator.getId())) {
            throw new IllegalArgumentException("You are not authorized to schedule this bus");
        }

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        Route reverseRoute = routeRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(
                        route.getDestination(), route.getSource())
                .orElseThrow(() -> new ResourceNotFoundException("Reverse route not found"));

        generateFutureSchedules(bus, route, reverseRoute, request.getJourneyStartDate(),
                request.getDepartureTime(), request.getArrivalTime(), request.getFare());
    }

    public void generateFutureSchedules(Bus bus, Route route, Route reverseRoute,
                                        LocalDate startDate,
                                        java.time.LocalTime departureTime,
                                        java.time.LocalTime arrivalTime,
                                        double fare) {

        for (int i = 0; i < 30; i++) {
            LocalDate date = startDate.plusDays(i);

            if (scheduleRepository.existsByBusIdAndJourneyDate(bus.getId(), date)) {
                continue; // Skip already scheduled date
            }

            Route currentRoute = (i % 2 == 0) ? route : reverseRoute;

            Schedule schedule = Schedule.builder()
                    .bus(bus)
                    .route(currentRoute)
                    .journeyDate(date)
                    .departureTime(departureTime)
                    .arrivalTime(arrivalTime)
                    .fare(fare)
                    .availableSeats(bus.getTotalSeats())
                    .status(Schedule.ScheduleStatus.SCHEDULED)
                    .isActive(true)
                    .build();

            scheduleRepository.save(schedule);
            generateSeatInventory(schedule, fare);
        }
    }

    private void generateSeatInventory(Schedule schedule, double fare) {
        List<Seat> busSeats = seatRepository.findByBus(schedule.getBus());

        List<SeatInventory> inventories = new ArrayList<>();
        for (Seat seat : busSeats) {
            SeatInventory inventory = SeatInventory.builder()
                    .seat(seat)
                    .schedule(schedule)
                    .seatFare(fare)
                    .isBooked(false)
                    .build();
            inventories.add(inventory);
        }

        seatInventoryRepository.saveAll(inventories);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    public void autoRenewSchedules() {
        List<Bus> activeBuses = busRepository.findByIsActiveTrue();

        for (Bus bus : activeBuses) {
            Schedule latestSchedule = scheduleRepository
                    .findTopByBusIdOrderByJourneyDateDesc(bus.getId())
                    .orElse(null);

            if (latestSchedule == null) continue;

            long daysRemaining = LocalDate.now().until(latestSchedule.getJourneyDate()).getDays();
            if (daysRemaining < 2) {
                Route route = latestSchedule.getRoute();
                Route reverseRoute = routeRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(
                        route.getDestination(), route.getSource()
                ).orElse(null);

                if (reverseRoute == null) continue;

                generateFutureSchedules(bus, route, reverseRoute,
                        latestSchedule.getJourneyDate().plusDays(1),
                        latestSchedule.getDepartureTime(),
                        latestSchedule.getArrivalTime(),
                        latestSchedule.getFare());
            }
        }
    }

    @Override
    public void cancelSchedule(Long scheduleId, String operatorEmail) {
        User operator = userRepository.findByEmail(operatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        if (!schedule.getBus().getOperator().getId().equals(operator.getId())) {
            throw new IllegalArgumentException("You are not authorized to cancel this schedule");
        }

        if (schedule.getStatus() != Schedule.ScheduleStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled trips can be cancelled");
        }

        // Hard delete seat inventory
        seatInventoryRepository.deleteAll(schedule.getSeatInventories());

        // Delete schedule
        scheduleRepository.delete(schedule);
    }

    @Override
    public List<ScheduleSummaryResponse> getSchedulesForBus(Long busId, String operatorEmail) {
        User operator = userRepository.findByEmail(operatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        if (!bus.getOperator().getId().equals(operator.getId())) {
            throw new IllegalArgumentException("You are not authorized to view this bus’s schedules");
        }

        List<Schedule> schedules = scheduleRepository.findByBusIdOrderByJourneyDateAsc(busId);

        return schedules.stream().map(schedule -> {
            Route route = schedule.getRoute();

            return ScheduleSummaryResponse.builder()
                    .scheduleId(schedule.getId())
                    .journeyDate(schedule.getJourneyDate())
                    .departureTime(schedule.getDepartureTime())
                    .arrivalTime(schedule.getArrivalTime())
                    .source(route.getSource())
                    .destination(route.getDestination())
                    .fare(schedule.getFare())
                    .availableSeats(schedule.getAvailableSeats())
                    .status(schedule.getStatus().name())
                    .build();
        }).toList();
    }


}
