package com.bus.reservation.service.impl;

import com.bus.reservation.dtos.*;
import com.bus.reservation.exception.BadRequestException;
import com.bus.reservation.exception.ResourceNotFoundException;
import com.bus.reservation.models.*;
import com.bus.reservation.repository.*;
import com.bus.reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final PaymentRepository paymentRepository;

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

    @Override
    public List<SeatViewResponse> getSeatLayout(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with ID: " + scheduleId));

        if (!schedule.getIsActive()) {
            throw new IllegalStateException("This schedule is not active for booking");
        }

        Long busId = schedule.getBus().getId();

        List<Seat> seats = seatRepository.findByBusId(busId);
        List<SeatInventory> inventoryList = seatInventoryRepository.findByScheduleId(scheduleId);

        Map<Long, SeatInventory> inventoryMap = inventoryList.stream()
                .collect(Collectors.toMap(inv -> inv.getSeat().getId(), inv -> inv));

        return seats.stream()
                .map(seat -> {
                    SeatInventory inventory = inventoryMap.get(seat.getId());
                    boolean isBooked = inventory != null && inventory.getIsBooked();
                    double fare = inventory != null ? inventory.getSeatFare() : schedule.getFare();

                    return SeatViewResponse.builder()
                            .seatId(seat.getId())
                            .seatNumber(seat.getSeatNumber())
                            .seatType(seat.getSeatType())
                            .isBooked(isBooked)
                            .fare(fare)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookSeatResponse bookSeat(BookSeatRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        if (!schedule.getIsActive() || !"Scheduled".equalsIgnoreCase(schedule.getStatus().name())) {
            throw new BadRequestException("This journey is not bookable");
        }

        List<String> seatNumbers = request.getSeatNumbers();
        List<PassengerRequest> passengers = request.getPassengers();

        if (seatNumbers == null || passengers == null || seatNumbers.isEmpty()) {
            throw new BadRequestException("Seats and passengers must be provided");
        }

        if (seatNumbers.size() > 6) {
            throw new BadRequestException("Cannot book more than 6 seats");
        }

        if (seatNumbers.size() != passengers.size()) {
            throw new BadRequestException("Number of seats and passengers must match");
        }

        Bus bus = schedule.getBus();
        List<Seat> busSeats = seatRepository.findByBusId(bus.getId());

        Map<String, Seat> seatMap = new HashMap<>();
        for (Seat seat : busSeats) {
            seatMap.put(seat.getSeatNumber(), seat);
        }

        List<SeatInventory> inventoriesToBook = new ArrayList<>();
        for (String seatNo : seatNumbers) {
            Seat seat = seatMap.get(seatNo);
            if (seat == null) {
                throw new BadRequestException("Invalid seat number: " + seatNo);
            }

            SeatInventory inventory = seatInventoryRepository
                    .findByScheduleIdAndSeatId(schedule.getId(), seat.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seat inventory not found for seat: " + seatNo));

            if (inventory.getIsBooked()) {
                throw new BadRequestException("Seat already booked: " + seatNo);
            }

            inventoriesToBook.add(inventory);
        }

        Booking booking = Booking.builder()
                .user(user)
                .schedule(schedule)
                .bookingTime(LocalDateTime.now())
                .bookingStatus(Booking.BookingStatus.CONFIRMED)
                .totalAmount(0.0)
                .paymentStatus(Booking.PaymentStatus.PENDING)
                .build();
        booking = bookingRepository.save(booking);

        List<BookingDetail> bookingDetails = new ArrayList<>();
        double totalFare = 0.0;

        for (int i = 0; i < inventoriesToBook.size(); i++) {
            SeatInventory inventory = inventoriesToBook.get(i);
            PassengerRequest passenger = passengers.get(i);

            inventory.setIsBooked(true);
            seatInventoryRepository.save(inventory);

            totalFare += inventory.getSeatFare();

            BookingDetail detail = BookingDetail.builder()
                    .booking(booking)
                    .seatInventory(inventory)
                    .passengerName(passenger.getName())
                    .passengerAge(passenger.getAge())
                    .gender(passenger.getGender())
                    .build();

            bookingDetails.add(detail);
        }
        bookingDetailRepository.saveAll(bookingDetails);

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(UUID.randomUUID().toString())
                .amount(totalFare)
                .paymentTime(LocalDateTime.now())
                .paymentStatus(Booking.PaymentStatus.PAID)
                .build();
        paymentRepository.save(payment);

        // Update booking with final fare
        booking.setTotalAmount(totalFare);
        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        bookingRepository.save(booking);

        // Update schedule seat availability
        schedule.setAvailableSeats(schedule.getAvailableSeats() - seatNumbers.size());
        scheduleRepository.save(schedule);

        // Build response
        return BookSeatResponse.builder()
                .bookingId(booking.getId())
                .bookedSeats(seatNumbers)
                .totalAmount(totalFare)
                .bookingStatus("CONFIRMED")
                .paymentStatus("PAID")
                .bookingTime(booking.getBookingTime())
                .message("Booking successful")
                .build();
    }
}
