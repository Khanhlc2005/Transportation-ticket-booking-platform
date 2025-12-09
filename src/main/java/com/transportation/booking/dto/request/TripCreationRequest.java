package com.transportation.booking.dto.request;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripCreationRequest {
    private String departure;
    private String destination;
    private LocalDateTime departureTime;
    private Double price;
    private int totalSeats;
    private String busOperator;
}