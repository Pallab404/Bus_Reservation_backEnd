package com.bus.reservation.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"source", "destination"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private  String destination;

    private Double distanceKm;

    @Column(nullable = false)
    private Integer estimatedDurationMins;

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<Schedule> schedules = new ArrayList<>();
}
