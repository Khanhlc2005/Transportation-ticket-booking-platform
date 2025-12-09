package com.transportation.booking.service;

import com.transportation.booking.dto.request.TripCreationRequest;
import com.transportation.booking.entity.Trip;
import com.transportation.booking.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;

    // Chỉ ADMIN mới được tạo chuyến xe
    @PreAuthorize("hasRole('ADMIN')")
    public Trip createTrip(TripCreationRequest request) {
        Trip trip = Trip.builder()
                .departure(request.getDeparture())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .price(request.getPrice())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats()) // Lúc mới tạo thì ghế trống = tổng ghế
                .build();

        return tripRepository.save(trip);
    }

    // Ai cũng xem được danh sách chuyến xe
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }
}