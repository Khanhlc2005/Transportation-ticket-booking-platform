package com.transportation.booking.service;

import com.transportation.booking.entity.Booking;
import com.transportation.booking.entity.Trip;
import com.transportation.booking.entity.User;
import com.transportation.booking.exception.AppException;
import com.transportation.booking.exception.ErrorCode;
import com.transportation.booking.repository.BookingRepository;
import com.transportation.booking.repository.TripRepository;
import com.transportation.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional // Quan trọng: Đảm bảo vừa tạo vé vừa trừ ghế thành công
    public Booking createBooking(Long tripId) {
        // 1. Lấy thông tin người đang đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Tìm chuyến xe
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // 3. Kiểm tra còn ghế trống không
        if (trip.getAvailableSeats() <= 0) {
            throw new RuntimeException("Chuyến xe đã hết ghế!");
        }

        // 4. Trừ đi 1 ghế
        trip.setAvailableSeats(trip.getAvailableSeats() - 1);
        tripRepository.save(trip);

        // 5. Lưu vé vào DB
        Booking booking = Booking.builder()
                .user(user)
                .trip(trip)
                .bookingTime(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        return bookingRepository.save(booking);
    }

    // Xem lịch sử đặt vé
    public List<Booking> getMyBookings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return bookingRepository.findAllByUser(user);
    }
}