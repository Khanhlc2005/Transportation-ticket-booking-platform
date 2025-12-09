package com.transportation.booking.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import com.transportation.booking.enums.Role;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "username", unique = true, nullable = false)
    String username;

    String password;
    String firstName;
    String lastName;
    LocalDate dob;

    @Enumerated(EnumType.STRING)
    Role role;
}