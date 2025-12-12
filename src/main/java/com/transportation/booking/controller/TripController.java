package com.transportation.booking.controller;

import com.transportation.booking.dto.request.TripCreationRequest;
import com.transportation.booking.dto.response.ApiResponse;
import com.transportation.booking.entity.Trip;
import com.transportation.booking.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;

    @PostMapping
    public ApiResponse<Trip> createTrip(@RequestBody TripCreationRequest request) {
        return ApiResponse.<Trip>builder()
                .result(tripService.createTrip(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<Trip>> getAllTrips() {
        return ApiResponse.<List<Trip>>builder()
                .result(tripService.getAllTrips())
                .build();
    }

    // API Xóa chuyến xe
    @DeleteMapping("/{tripId}")
    public ApiResponse<String> deleteTrip(@PathVariable Long tripId) {
        tripService.deleteTrip(tripId);
        return ApiResponse.<String>builder()
                .result("Chuyến xe đã được xóa thành công")
                .build();
    }
}