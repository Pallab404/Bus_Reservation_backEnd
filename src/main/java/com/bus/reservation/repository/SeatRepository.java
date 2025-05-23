package com.bus.reservation.repository;

import com.bus.reservation.models.Bus;
import com.bus.reservation.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByBus(Bus bus);

    boolean existsBySeatNumberAndBus(String seatNumber, Bus bus);
}
