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
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") // Liên kết với bảng User
    User user;

    @ManyToOne
    @JoinColumn(name = "trip_id") // Liên kết với bảng Trip
    Trip trip;

    LocalDateTime bookingTime; // Thời gian đặt vé
    String status; // Trạng thái: CONFIRMED (Thành công)
}