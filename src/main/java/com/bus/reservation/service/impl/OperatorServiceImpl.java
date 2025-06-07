package com.bus.reservation.service.impl;

import com.bus.reservation.dtos.ScheduleBookingInfoResponse;
import com.bus.reservation.exception.ResourceNotFoundException;
import com.bus.reservation.models.*;
import com.bus.reservation.repository.BookingDetailRepository;
import com.bus.reservation.repository.BookingRepository;
import com.bus.reservation.repository.ScheduleRepository;
import com.bus.reservation.repository.UserRepository;
import com.bus.reservation.service.OperatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperatorServiceImpl implements OperatorService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;

    @Override
    public List<ScheduleBookingInfoResponse> getBookingsForSchedule(Long scheduleId, String operatorEmail) {

        User operator = userRepository.findByEmail(operatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        if (!schedule.getBus().getOperator().getId().equals(operator.getId())) {
            throw new ResourceNotFoundException("This schedule does not belong to the operator");
        }
        List<Booking> bookings = bookingRepository.findByScheduleId(scheduleId);

        return bookings.stream().map(booking -> {
            List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());

            List<ScheduleBookingInfoResponse.PassengerInfo> passengers = details.stream().map(detail -> {
                SeatInventory inventory = detail.getSeatInventory();
                Seat seat = inventory.getSeat();

                return ScheduleBookingInfoResponse.PassengerInfo.builder()
                        .seatNumber(seat.getSeatNumber())
                        .name(detail.getPassengerName())
                        .age(detail.getPassengerAge())
                        .gender(detail.getGender())
                        .build();
            }).toList();

            User user = booking.getUser();

            return ScheduleBookingInfoResponse.builder()
                    .bookingId(booking.getId())
                    .customerName(user.getFirstName() + " " + user.getLastName())
                    .customerEmail(user.getEmail())
                    .bookingStatus(booking.getBookingStatus().name())
                    .paymentStatus(booking.getPaymentStatus().name())
                    .seatCount(passengers.size())
                    .totalAmount(booking.getTotalAmount())
                    .bookingTime(booking.getBookingTime())
                    .passengers(passengers)
                    .build();

        }).collect(Collectors.toList());



    }
}
