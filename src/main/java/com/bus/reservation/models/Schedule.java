package com.bus.reservation.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate journeyDate;

    private LocalTime departureTime;

    private LocalTime arrivalTime;

    private Integer availableSeats;

    private Double fare;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status; // e.g., SCHEDULED, CANCELLED, COMPLETED

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Route route;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<SeatInventory> seatInventories = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    public enum ScheduleStatus {
        SCHEDULED, CANCELLED, COMPLETED
    }

}
