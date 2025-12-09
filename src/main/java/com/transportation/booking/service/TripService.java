package com.transportation.booking.service;

import com.transportation.booking.dto.request.TripCreationRequest;
import com.transportation.booking.entity.Booking; // <--- Import
import com.transportation.booking.entity.Trip;
import com.transportation.booking.repository.BookingRepository; // <--- Import
import com.transportation.booking.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;

    // 1. Cần thêm cái này để lấy danh sách vé đã đặt
    private final BookingRepository bookingRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public Trip createTrip(TripCreationRequest request) {
        Trip trip = Trip.builder()
                .departure(request.getDeparture())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .price(request.getPrice())
                .busOperator(request.getBusOperator())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .build();

        return tripRepository.save(trip);
    }

    // 2. Sửa lại hàm này để "bơm" dữ liệu ghế đã bán vào cho Frontend
    public List<Trip> getAllTrips() {
        List<Trip> trips = tripRepository.findAll();

        // Duyệt qua từng chuyến xe
        for (Trip trip : trips) {
            // Tìm tất cả vé của chuyến này
            List<Booking> bookings = bookingRepository.findAllByTrip(trip);

            // Lọc ra danh sách tên ghế (ví dụ: ["A01", "A05"])
            List<String> bookedSeatNumbers = bookings.stream()
                    .map(Booking::getSeatNumber)
                    .toList();

            // Gán vào biến tạm (Transient) để trả về cho Frontend
            trip.setBookedSeats(bookedSeatNumbers);
        }

        return trips;
    }
}