package com.bus.reservation.service.impl;

import com.bus.reservation.dtos.AddBusRequest;
import com.bus.reservation.dtos.BusSummaryResponse;
import com.bus.reservation.exception.ResourceAlreadyExistsException;
import com.bus.reservation.exception.ResourceNotFoundException;
import com.bus.reservation.models.Bus;
import com.bus.reservation.models.Seat;
import com.bus.reservation.models.User;
import com.bus.reservation.repository.BusRepository;
import com.bus.reservation.repository.ScheduleRepository;
import com.bus.reservation.repository.SeatRepository;
import com.bus.reservation.repository.UserRepository;
import com.bus.reservation.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusServiceImpl implements BusService {
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public void addBus(AddBusRequest request, String operatorEmail) {
        if (busRepository.existsByBusNumber(request.getBusNumber())) {
            throw new ResourceAlreadyExistsException("Bus number already registered");
        }

        User operator = userRepository.findByEmail(operatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        if (!User.Role.OPERATOR.equals(operator.getRole())) {
            throw new IllegalArgumentException("Only operators can add buses");
        }

        Bus bus = Bus.builder()
                .busName(request.getBusName())
                .busNumber(request.getBusNumber())
                .busType(request.getBusType())
                .totalSeats(request.getTotalSeats())
                .operator(operator)
                .isActive(true)
                .build();

        busRepository.save(bus);
        generateSeatsForBus(bus);
    }

    private void generateSeatsForBus(Bus bus) {
        List<Seat> seats = new ArrayList<>();
        String[] seatTypes = {"Window", "AISLE", "MIDDLE"};

        for (int i = 1; i <= bus.getTotalSeats(); i++) {
            String seatNumber = "S" + i;
            String seatType = seatTypes[(i-1) % seatTypes.length];

            Seat seat = Seat.builder()
                    .bus(bus)
                    .seatNumber(seatNumber)
                    .seatType(seatType)
                    .build();
            seats.add(seat);
        }
        seatRepository.saveAll(seats);
    }

    @Override
    public List<BusSummaryResponse> getBusesForOperator(String email) {
        User operator = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        List<Bus> buses = busRepository.findByOperator(operator);
        return buses.stream().map(bus -> {
            boolean scheduled = scheduleRepository.existsByBusId(bus.getId());
            return BusSummaryResponse.builder()
                    .id(bus.getId())
                    .busName(bus.getBusName())
                    .busNumber(bus.getBusNumber())
                    .busType(bus.getBusType())
                    .totalSeats(bus.getTotalSeats())
                    .scheduled(scheduled)
                    .build();
        }).toList();
    }
}