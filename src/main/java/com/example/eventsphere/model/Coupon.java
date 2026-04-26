package com.example.eventsphere.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    private String description;
    private int discountPercent;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    private LocalDate expiryDate;

    @Builder.Default
    private boolean active = true;

    private int usageCount;
    private int maxUsage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public boolean isValid() {
        return active && (expiryDate == null || !expiryDate.isBefore(LocalDate.now()))
                && (maxUsage <= 0 || usageCount < maxUsage);
    }

    public BigDecimal apply(BigDecimal amount) {
        BigDecimal discount = amount.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
        if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) discount = maxDiscount;
        return amount.subtract(discount);
    }
}
