package com.transportation.booking.controller;

import com.transportation.booking.dto.response.ApiResponse;
import com.transportation.booking.entity.Booking;
import com.transportation.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    // API Đặt vé (POST)
    @PostMapping("/{tripId}")
    public ApiResponse<Booking> bookTicket(@PathVariable Long tripId) {
        return ApiResponse.<Booking>builder()
                .result(bookingService.createBooking(tripId))
                .build();
    }

    // API Xem lịch sử vé (GET)
    @GetMapping("/my-bookings")
    public ApiResponse<List<Booking>> getMyBookings() {
        return ApiResponse.<List<Booking>>builder()
                .result(bookingService.getMyBookings())
                .build();
    }
}