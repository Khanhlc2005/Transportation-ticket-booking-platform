package com.transportation.booking.service;

import com.transportation.booking.dto.request.BookingRequest;
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

    @Transactional
    public Booking createBooking(Long tripId, BookingRequest request) {
        // 1. Lấy thông tin người đang đăng nhập (Chủ tài khoản)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Tìm chuyến xe
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // 3. Kiểm tra ghế này đã có ai đặt chưa?
        boolean isSeatTaken = bookingRepository.findAllByTrip(trip).stream()
                .anyMatch(booking -> booking.getSeatNumber() != null &&
                        booking.getSeatNumber().equals(request.getSeatNumber()));

        if (isSeatTaken) {
            throw new RuntimeException("Ghế " + request.getSeatNumber() + " đã có người đặt!");
        }

        // 4. Kiểm tra còn ghế trống không
        if (trip.getAvailableSeats() <= 0) {
            throw new RuntimeException("Chuyến xe đã hết ghế!");
        }

        // 5. Trừ đi 1 ghế
        trip.setAvailableSeats(trip.getAvailableSeats() - 1);
        tripRepository.save(trip);

        // --- XỬ LÝ THÔNG TIN NGƯỜI ĐI ---
        // Nếu Request có gửi tên khách (do người dùng sửa), thì lấy tên đó.
        // Nếu không, lấy tên của chủ tài khoản.
        String finalName = (request.getPassengerName() != null && !request.getPassengerName().isEmpty())
                ? request.getPassengerName()
                : (user.getLastName() + " " + user.getFirstName());

        String finalPhone = (request.getPassengerPhone() != null && !request.getPassengerPhone().isEmpty())
                ? request.getPassengerPhone()
                : user.getPhone();
        // --------------------------------

        // 6. Lưu vé vào DB
        Booking booking = Booking.builder()
                .user(user)
                .trip(trip)
                .seatNumber(request.getSeatNumber())
                .passengerName(finalName)   // Lưu tên khách thực sự
                .passengerPhone(finalPhone) // Lưu SĐT khách thực sự
                .bookingTime(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        return bookingRepository.save(booking);
    }

    public List<Booking> getMyBookings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return bookingRepository.findAllByUser(user);
    }


    @Transactional
    public void cancelBooking(Long bookingId) {
        // 1. Lấy user đang đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Tìm vé
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Vé không tồn tại"));

        // 3. Check xem vé này có phải của user đó không?
        if (!booking.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền hủy vé này!");
        }

        // 4. Trả lại ghế cho chuyến xe (Cộng thêm 1)
        Trip trip = booking.getTrip();
        trip.setAvailableSeats(trip.getAvailableSeats() + 1);
        tripRepository.save(trip);

        // 5. Xóa vé vĩnh viễn
        bookingRepository.delete(booking);
    }
}
