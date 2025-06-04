package com.bus.reservation.service.impl;

import com.bus.reservation.dtos.*;
import com.bus.reservation.exception.BadRequestException;
import com.bus.reservation.exception.ResourceNotFoundException;
import com.bus.reservation.models.*;
import com.bus.reservation.repository.*;
import com.bus.reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
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
        log.info("Starting seat booking process for request: {}", request);

        try {
            // 1. Validate user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found with email: {}", email);
                        return new ResourceNotFoundException("Operator not found");
                    });
            log.debug("Found user: {}", user.getId());

            // 2. Validate schedule
            Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> {
                        log.error("Schedule not found with ID: {}", request.getScheduleId());
                        return new ResourceNotFoundException("Schedule not found");
                    });
            log.debug("Found schedule: {} (Status: {})", schedule.getId(), schedule.getStatus());

            // 3. Check schedule availability
            if (!schedule.getIsActive() || !"Scheduled".equalsIgnoreCase(schedule.getStatus().name())) {
                log.warn("Schedule {} is not bookable. Active: {}, Status: {}",
                        schedule.getId(), schedule.getIsActive(), schedule.getStatus());
                throw new BadRequestException("This journey is not bookable");
            }

            // 4. Validate seats and passengers
            List<String> seatNumbers = request.getSeatNumbers();
            List<PassengerRequest> passengers = request.getPassengers();

            if (seatNumbers == null || passengers == null || seatNumbers.isEmpty()) {
                log.error("Invalid seat/passenger data - Seats: {}, Passengers: {}", seatNumbers, passengers);
                throw new BadRequestException("Seats and passengers must be provided");
            }

            if (seatNumbers.size() > 6) {
                log.error("Too many seats requested: {}", seatNumbers.size());
                throw new BadRequestException("Cannot book more than 6 seats");
            }

            if (seatNumbers.size() != passengers.size()) {
                log.error("Seat-passenger mismatch. Seats: {}, Passengers: {}", seatNumbers.size(), passengers.size());
                throw new BadRequestException("Number of seats and passengers must match");
            }

            // 5. Get bus and seats
            Bus bus = schedule.getBus();
            if (bus == null) {
                log.error("No bus assigned to schedule {}", schedule.getId());
                throw new IllegalStateException("No bus assigned to this schedule");
            }
            log.debug("Processing bus: {}", bus.getId());

            List<Seat> busSeats = seatRepository.findByBusId(bus.getId());
            if (busSeats.isEmpty()) {
                log.error("No seats found for bus {}", bus.getId());
                throw new ResourceNotFoundException("No seats configured for this bus");
            }

            // 6. Prepare seat inventory
            Map<String, Seat> seatMap = busSeats.stream()
                    .collect(Collectors.toMap(Seat::getSeatNumber, Function.identity()));

            List<SeatInventory> inventoriesToBook = new ArrayList<>();
            for (String seatNo : seatNumbers) {
                try {
                    Seat seat = seatMap.get(seatNo);
                    if (seat == null) {
                        log.error("Invalid seat number: {}", seatNo);
                        throw new BadRequestException("Invalid seat number: " + seatNo);
                    }

                    SeatInventory inventory = seatInventoryRepository
                            .findByScheduleIdAndSeatId(schedule.getId(), seat.getId())
                            .orElseThrow(() -> {
                                log.error("Inventory missing for seat {} on schedule {}", seatNo, schedule.getId());
                                return new ResourceNotFoundException("Seat inventory not found for seat: " + seatNo);
                            });

                    if (inventory.getIsBooked()) {
                        log.warn("Seat already booked: {}", seatNo);
                        throw new BadRequestException("Seat already booked: " + seatNo);
                    }

                    inventoriesToBook.add(inventory);
                } catch (Exception e) {
                    log.error("Failed processing seat {}: {}", seatNo, e.getMessage());
                    throw e;
                }
            }

            // 7. Create booking
            Booking booking = Booking.builder()
                    .user(user)
                    .schedule(schedule)
                    .bookingTime(LocalDateTime.now())
                    .bookingStatus(Booking.BookingStatus.CONFIRMED)
                    .totalAmount(0.0)
                    .paymentStatus(Booking.PaymentStatus.PENDING)
                    .build();

            try {
                booking = bookingRepository.save(booking);
                log.debug("Created booking: {}", booking.getId());
            } catch (Exception e) {
                log.error("Failed to save booking: {}", e.getMessage());
                throw new RuntimeException("Failed to create booking record", e);
            }

            // 8. Process booking details
            List<BookingDetail> bookingDetails = new ArrayList<>();
            double totalFare = 0.0;

            for (int i = 0; i < inventoriesToBook.size(); i++) {
                try {
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
                } catch (Exception e) {
                    log.error("Failed processing passenger {}: {}", i, e.getMessage());
                    throw new RuntimeException("Failed to process passenger " + i, e);
                }
            }

            try {
                bookingDetailRepository.saveAll(bookingDetails);
                log.debug("Saved {} booking details", bookingDetails.size());
            } catch (Exception e) {
                log.error("Failed to save booking details: {}", e.getMessage());
                throw new RuntimeException("Failed to save booking details", e);
            }

            // 9. Process payment
            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod(request.getPaymentMethod())
                    .transactionId(UUID.randomUUID().toString())
                    .amount(totalFare)
                    .paymentTime(LocalDateTime.now())
                    .paymentStatus(Booking.PaymentStatus.PAID)
                    .build();

            try {
                paymentRepository.save(payment);
                log.debug("Processed payment: {}", payment.getTransactionId());
            } catch (Exception e) {
                log.error("Payment failed: {}", e.getMessage());
                throw new RuntimeException("Payment processing failed", e);
            }

            // 10. Finalize booking
            booking.setTotalAmount(totalFare);
            booking.setPaymentStatus(Booking.PaymentStatus.PAID);
            bookingRepository.save(booking);

            // 11. Update schedule
            schedule.setAvailableSeats(schedule.getAvailableSeats() - seatNumbers.size());
            scheduleRepository.save(schedule);

            log.info("Booking completed successfully. Booking ID: {}", booking.getId());

            return BookSeatResponse.builder()
                    .bookingId(booking.getId())
                    .bookedSeats(seatNumbers)
                    .totalAmount(totalFare)
                    .bookingStatus("CONFIRMED")
                    .paymentStatus("PAID")
                    .bookingTime(booking.getBookingTime())
                    .message("Booking successful")
                    .build();

        } catch (Exception e) {
            log.error("CRITICAL ERROR IN BOOKING PROCESS: ", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Booking failed: " + e.getMessage(),
                    e
            );
        }
    }
    @Override
    public List<BookingCardResponse> getMyBookings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        List<BookingCardResponse> responses = new ArrayList<>();

        for (Booking booking : bookings) {
            Schedule schedule = booking.getSchedule();
            Route route = schedule.getRoute();
            Bus bus = schedule.getBus();

            LocalDate journeyDate = schedule.getJourneyDate();
            LocalTime departure = schedule.getDepartureTime();
            LocalTime arrival = schedule.getArrivalTime();

            String bookingStatus;
            boolean isCancelable = false;

            if ("CANCELLED".equalsIgnoreCase(booking.getBookingStatus().name())) {
                bookingStatus = "CANCELLED";
            } else if (LocalDate.now().isAfter(journeyDate) ||
                    (LocalDate.now().isEqual(journeyDate) && LocalTime.now().isAfter(arrival))) {
                bookingStatus = "COMPLETED";
            } else {
                bookingStatus = "CONFIRMED";

                // Check if cancellation is allowed
                LocalDate today = LocalDate.now();
                if (journeyDate.isAfter(today) ||
                        (journeyDate.isEqual(today) && departure.minusHours(5).isAfter(LocalTime.now()))) {
                    isCancelable = true;
                }
            }
            List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(booking.getId());

            List<PassengerInfo> passengerInfos = bookingDetails.stream().map(detail -> {
                SeatInventory inventory = detail.getSeatInventory();
                Seat seat = inventory.getSeat();

                return PassengerInfo.builder()
                        .seatNumber(seat.getSeatNumber())
                        .name(detail.getPassengerName())
                        .age(detail.getPassengerAge())
                        .gender(detail.getGender())
                        .build();
            }).toList();

            responses.add(BookingCardResponse.builder()
                    .bookingId(booking.getId())
                    .busName(bus.getBusName())
                    .busNumber(bus.getBusNumber())
                    .source(route.getSource())
                    .destination(route.getDestination())
                    .journeyDate(journeyDate)
                    .departureTime(departure)
                    .arrivalTime(arrival)
                    .bookingStatus(bookingStatus)
                    .isCancelable(isCancelable)
                    .passengers(passengerInfos)
                    .build());
        }

        return responses;
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("Unauthorized to cancel this booking");
        }

        if (!booking.getBookingStatus().equals(Booking.BookingStatus.CONFIRMED)) {
                throw new BadRequestException("Booking is not in a cancellable state");
        }

        Schedule schedule = booking.getSchedule();
        LocalDateTime departure = LocalDateTime.of(schedule.getJourneyDate(), schedule.getDepartureTime());

        if (departure.isBefore(LocalDateTime.now().plusHours(5))) {
                throw new BadRequestException("Cancellation not allowed within 5 hours of departure");
        }

        // Update booking and payment
        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        booking.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
        bookingRepository.save(booking);

        // Release seats and update inventory
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);
        int releasedSeats = 0;

        for (BookingDetail detail : details) {
            SeatInventory inventory = detail.getSeatInventory();
            if (inventory.getIsBooked()) {
                inventory.setIsBooked(false);
                seatInventoryRepository.save(inventory);
                releasedSeats++;
                }
            }

        // Update available seats
        schedule.setAvailableSeats(schedule.getAvailableSeats() + releasedSeats);
        scheduleRepository.save(schedule);
    }
}
