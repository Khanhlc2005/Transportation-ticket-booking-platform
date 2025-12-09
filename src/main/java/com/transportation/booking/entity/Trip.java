package com.transportation.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List; // <--- Nhớ import dòng này

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "trips")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String departure;
    String destination;

    LocalDateTime departureTime;

    @Column(name = "bus_operator")
    String busOperator;

    Double price;

    int totalSeats;
    int availableSeats;

    // TÔ MÀU GHẾ ---
    @Transient
            List<String> bookedSeats;
}