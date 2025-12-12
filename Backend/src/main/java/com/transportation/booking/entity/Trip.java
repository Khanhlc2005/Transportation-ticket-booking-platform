package com.transportation.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

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

    String departure; // Điểm đi (VD: Hà Nội)
    String destination; // Điểm đến (VD: Đà Nẵng)

    LocalDateTime departureTime; // Thời gian khởi hành

    Double price; // Giá vé

    int totalSeats; // Tổng số ghế
    int availableSeats; // Số ghế còn trống
}