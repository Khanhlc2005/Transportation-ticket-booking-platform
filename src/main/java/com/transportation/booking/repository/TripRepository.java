package com.transportation.booking.repository;

import com.transportation.booking.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    // Tìm chuyến xe theo điểm đi và đến (để User tìm kiếm)
    List<Trip> findByDepartureAndDestination(String departure, String destination);
}