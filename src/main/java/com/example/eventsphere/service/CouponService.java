package com.example.eventsphere.service;

import com.example.eventsphere.model.Coupon;
import com.example.eventsphere.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    public List<Coupon> findAll() {
        return couponRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Coupon> findActive() {
        return couponRepository.findByActiveTrue();
    }

    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code);
    }

    public BigDecimal apply(String code, BigDecimal amount) {
        return findByCode(code).filter(Coupon::isValid).map(c -> c.apply(amount)).orElse(amount);
    }

    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    public void delete(Long id) {
        couponRepository.deleteById(id);
    }
}
