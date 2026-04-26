package com.example.eventsphere.service;

import com.example.eventsphere.enums.BookingStatus;
import com.example.eventsphere.model.*;
import com.example.eventsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    public List<Booking> findAll()                         { return bookingRepository.findAllByOrderByCreatedAtDesc(); }
    public List<Booking> findByUser(User user)             { return bookingRepository.findByUserOrderByCreatedAtDesc(user); }
    public List<Booking> findByEvent(Event event)          { return bookingRepository.findByEvent(event); }
    public Optional<Booking> findById(Long id)             { return bookingRepository.findById(id); }
    public Optional<Booking> findByRef(String ref) { return bookingRepository.findByBookingRef(ref); }
    public long countAll()                                 { return bookingRepository.count(); }
    public long countByStatus(BookingStatus status)        { return bookingRepository.countByStatus(status); }
    public BigDecimal totalRevenue()                       { return bookingRepository.sumTotalRevenue(); }
    public BigDecimal revenueForOrganizer(User organizer)  { return bookingRepository.sumRevenueForOrganizer(organizer); }

    @Transactional
    public Booking save(Booking booking) { return bookingRepository.save(booking); }

    @Transactional
    public void cancel(Long id) {
        bookingRepository.findById(id).ifPresent(b -> {
            b.setStatus(BookingStatus.CANCELLED);
            // restore seat
            Event e = b.getEvent();
            e.setAvailableSeats(e.getAvailableSeats() + b.getQuantity());
            bookingRepository.save(b);
        });
    }
}
