package com.transportation.booking.dto.request;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String seatNumber;
    private String passengerName;
    private String passengerPhone;
}