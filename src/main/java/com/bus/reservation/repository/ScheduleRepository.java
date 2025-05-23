package com.bus.reservation.repository;

import com.bus.reservation.models.Schedule;
import com.bus.reservation.models.Schedule.ScheduleStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    boolean existsByBusId(Long busId);

    List<Schedule> findByBusId(Long busId);

    List<Schedule> findByBusIdAndStatus(Long busId, ScheduleStatus status);

    List<Schedule> findByBusIdAndJourneyDateAfter(Long busId, LocalDate date);

    List<Schedule> findByIsActiveTrueAndJourneyDateBetween(LocalDate startDate, LocalDate endDate);

    void deleteByBusIdAndStatus(Long busId, ScheduleStatus status);

    boolean existsByBusIdAndJourneyDate(Long busId, LocalDate journeyDate);

    List<Schedule> findByJourneyDate(LocalDate date);

    Optional<Schedule> findTopByBusIdOrderByJourneyDateDesc(Long id);

    // ScheduleRepository.java
    List<Schedule> findByJourneyDateBeforeAndStatus(LocalDate date, Schedule.ScheduleStatus status);

    List<Schedule> findByBusIdOrderByJourneyDateAsc(Long busId);

    List<Schedule> findByRouteIdAndJourneyDateAndIsActiveTrue(Long routeId, LocalDate journeyDate);
}

