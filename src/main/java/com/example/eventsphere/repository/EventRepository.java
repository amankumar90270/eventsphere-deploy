package com.example.eventsphere.repository;

import com.example.eventsphere.enums.EventStatus;
import com.example.eventsphere.model.Category;
import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(User organizer);
    List<Event> findByStatus(EventStatus status);
    List<Event> findByStatusIn(List<EventStatus> statuses);
    List<Event> findByFeaturedTrueAndStatus(EventStatus status);
    List<Event> findByFeaturedTrueAndStatusIn(List<EventStatus> statuses);
    List<Event> findByCategory(Category category);
    List<Event> findByOrganizerAndStatus(User organizer, EventStatus status);
    List<Event> findAllByOrderByCreatedAtDesc();

    long countByStatus(EventStatus status);
    long countByOrganizer(User organizer);

    @Query("SELECT COALESCE(SUM(b.totalAmount),0) FROM Booking b WHERE b.event.organizer = :organizer AND b.status = 'CONFIRMED'")
    BigDecimal sumRevenueByOrganizer(User organizer);
}
