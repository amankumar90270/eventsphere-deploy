package com.example.eventsphere.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tickets")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String ticketType;   // "General", "VIP", "Premium", "Student"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private int totalQuantity;
    private int availableQuantity;

    private String benefits;     // comma-separated perks

    @Builder.Default
    private boolean active = true;

    public String getFormattedPrice() {
        return price.compareTo(BigDecimal.ZERO) == 0
                ? "Free" : "₹" + String.format("%,.0f", price);
    }
}
