package com.bus.reservation.repository;

import com.bus.reservation.models.SeatInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {
    List<SeatInventory> findByScheduleId(Long scheduleId);

    boolean existsByScheduleId(Long scheduleId);

    Optional<SeatInventory> findByScheduleIdAndSeatId(Long scheduleId, Long seatId);
}
