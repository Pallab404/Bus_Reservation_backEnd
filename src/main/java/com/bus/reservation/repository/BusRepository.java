package com.bus.reservation.repository;

import com.bus.reservation.models.Bus;
import com.bus.reservation.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {
    boolean existsByBusNumber(String busNumber);
    Optional<Bus> findByBusNumber(String busNumber);

    List<Bus> findByOperator(User operator);

    List<Bus> findByIsActiveTrue();
}
