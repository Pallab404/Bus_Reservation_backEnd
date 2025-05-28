package com.bus.reservation.repository;

import com.bus.reservation.models.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);

    boolean existsBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);

    @Query("SELECT DISTINCT r.source FROM Route r")
    List<String> findDistinctSources();

    @Query("SELECT DISTINCT r.destination FROM Route r")
    List<String> findDistinctDestinations();
}
