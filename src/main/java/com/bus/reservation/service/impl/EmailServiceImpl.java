package com.bus.reservation.service.impl;

import com.bus.reservation.models.Booking;
import com.bus.reservation.models.BookingDetail;
import com.bus.reservation.models.User;
import com.bus.reservation.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendBookingConfirmation(User user, Booking booking, List<BookingDetail> bookingDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(user.getEmail());
            helper.setSubject("Booking Confirmation - ID: " + booking.getId());

            StringBuilder content = new StringBuilder();
            content.append("<h2>Booking Confirmed!</h2>")
                    .append("<p><strong>Booking ID:</strong> ").append(booking.getId()).append("</p>")
                    .append("<p><strong>Journey Date:</strong> ").append(booking.getSchedule().getJourneyDate()).append("</p>")
                    .append("<p><strong>Departure Time:</strong> ").append(booking.getSchedule().getDepartureTime()).append("</p>")
                    .append("<p><strong>From:</strong> ").append(booking.getSchedule().getRoute().getSource()).append("</p>")
                    .append("<p><strong>To:</strong> ").append(booking.getSchedule().getRoute().getDestination()).append("</p>")
                    .append("<p><strong>Bus:</strong> ").append(booking.getSchedule().getBus().getBusNumber()).append("</p>")
                    .append("<p><strong>Total Fare:</strong> ₹").append(booking.getTotalAmount()).append("</p>")
                    .append("<hr/><h4>Passenger Details:</h4><ul>");

            for (BookingDetail detail : bookingDetails) {
                content.append("<li>")
                        .append(detail.getPassengerName())
                        .append(" (").append(detail.getGender()).append(", Age ")
                        .append(detail.getPassengerAge()).append(") - Seat ")
                        .append(detail.getSeatInventory().getSeat().getSeatNumber())
                        .append("</li>");
            }
            content.append("</ul>");

            helper.setText(content.toString(), true);
            mailSender.send(message);
        } catch (Exception e) {
            // Log but do not block booking flow
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }
}

