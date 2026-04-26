package com.example.eventsphere.model;

import com.example.eventsphere.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    private String venue;
    private String city;
    private String address;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal ticketPrice = BigDecimal.ZERO;

    private int totalCapacity;
    private int availableSeats;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventStatus status = EventStatus.PENDING;

    private String imageUrl;
    private String tags;

    @Builder.Default
    private boolean featured = false;

    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.availableSeats == 0) this.availableSeats = this.totalCapacity;
    }

    public boolean isFree() {
        return ticketPrice == null || ticketPrice.compareTo(BigDecimal.ZERO) == 0;
    }

    public String getFormattedPrice() {
        return isFree() ? "Free" : "₹" + String.format("%,.0f", ticketPrice);
    }

    public int getBookedSeats() {
        return totalCapacity - availableSeats;
    }

    public double getOccupancyPercent() {
        return totalCapacity == 0 ? 0 : (getBookedSeats() * 100.0 / totalCapacity);
    }
}
