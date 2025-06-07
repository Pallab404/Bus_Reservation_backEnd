package com.bus.reservation.service;

import com.bus.reservation.dtos.ScheduleBookingInfoResponse;

import java.util.List;

public interface OperatorService {
    List<ScheduleBookingInfoResponse> getBookingsForSchedule(Long scheduleId, String operatorEmail);

}
