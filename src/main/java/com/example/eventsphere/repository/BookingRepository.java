package com.example.eventsphere.repository;

import com.example.eventsphere.enums.BookingStatus;
import com.example.eventsphere.model.Booking;
import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    List<Booking> findByEvent(Event event);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findAllByOrderByCreatedAtDesc();
    long countByStatus(BookingStatus status);
    long countByEvent(Event event);

    @Query("SELECT COALESCE(SUM(b.totalAmount),0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(b.totalAmount),0) FROM Booking b WHERE b.event.organizer = :organizer AND b.status = 'CONFIRMED'")
    BigDecimal sumRevenueForOrganizer(User organizer);
    java.util.Optional<Booking> findByBookingRef(String bookingRef);
}