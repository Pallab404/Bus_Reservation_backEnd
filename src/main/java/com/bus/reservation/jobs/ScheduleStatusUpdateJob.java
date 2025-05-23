package com.bus.reservation.jobs;

import com.bus.reservation.models.Schedule;
import com.bus.reservation.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleStatusUpdateJob {

    private final ScheduleRepository scheduleRepository;

    // Runs every day at 1 AM
    @Scheduled(cron = "0 0 1 * * ?")
    public void markCompletedSchedules() {
        LocalDate today = LocalDate.now();

        List<Schedule> pastSchedules = scheduleRepository
                .findByJourneyDateBeforeAndStatus(today, Schedule.ScheduleStatus.SCHEDULED);

        for (Schedule schedule : pastSchedules) {
            schedule.setStatus(Schedule.ScheduleStatus.COMPLETED);
        }

        scheduleRepository.saveAll(pastSchedules);

        log.info("Marked {} schedules as COMPLETED", pastSchedules.size());
    }
}
