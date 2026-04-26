package com.example.eventsphere.service;

import com.example.eventsphere.model.Payment;
import com.example.eventsphere.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    public List<Payment> findAll()   { return paymentRepository.findAllByOrderByCreatedAtDesc(); }
    public Payment save(Payment p)   { return paymentRepository.save(p); }
}
