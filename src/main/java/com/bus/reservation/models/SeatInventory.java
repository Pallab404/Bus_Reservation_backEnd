package com.bus.reservation.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean isBooked = false;

    private Double seatFare;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Seat seat;

    @OneToOne(mappedBy = "seatInventory", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private BookingDetail bookingDetail;

    @PrePersist
    @PreUpdate
    private void validateFare() {
        if (this.seatFare == null && this.schedule != null) {
            this.seatFare = this.schedule.getFare();
        }
    }

}
