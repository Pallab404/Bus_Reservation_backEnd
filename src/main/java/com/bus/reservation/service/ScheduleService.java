package com.bus.reservation.service;

import com.bus.reservation.dtos.CreateScheduleRequest;
import com.bus.reservation.dtos.ScheduleSummaryResponse;

import java.util.List;

public interface ScheduleService {
    void createSchedule(CreateScheduleRequest request, String operatorEmail);
    void autoRenewSchedules();
    void cancelSchedule(Long scheduleId, String operatorEmail);
    List<ScheduleSummaryResponse> getSchedulesForBus(Long busId, String operatorEmail);
}
