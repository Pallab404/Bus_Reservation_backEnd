package com.bus.reservation.service;

import com.bus.reservation.models.Booking;
import com.bus.reservation.models.BookingDetail;
import com.bus.reservation.models.User;

import java.util.List;


public interface EmailService {
    void sendBookingConfirmation(User user, Booking booking, List<BookingDetail> bookingDetails);
}
