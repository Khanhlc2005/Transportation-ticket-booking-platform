package com.transportation.booking.repository;

import com.transportation.booking.entity.Booking;
import com.transportation.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Tìm danh sách vé của một người dùng cụ thể
    List<Booking> findAllByUser(User user);
}