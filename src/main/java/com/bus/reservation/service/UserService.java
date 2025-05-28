package com.bus.reservation.service;

import com.bus.reservation.dtos.*;

import java.util.List;

public interface UserService {
    List<BusSearchResponse> searchScheduledBuses(BusSearchRequest request, String userEmail);

    List<SeatViewResponse> getSeatLayout(Long scheduleId);

    BookSeatResponse bookSeat(BookSeatRequest request, String email);
}
