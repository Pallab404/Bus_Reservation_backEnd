package com.bus.reservation.repository;

import com.bus.reservation.models.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);

    boolean existsBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
}
