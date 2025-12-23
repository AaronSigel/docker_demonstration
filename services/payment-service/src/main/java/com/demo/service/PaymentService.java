package com.demo.service;

import com.demo.model.Payment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PaymentService {
    private final Map<Long, Payment> payments = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Payment createPayment(Long bookingId, Double amount) {
        Long id = idGenerator.getAndIncrement();
        Payment payment = new Payment(
            id,
            bookingId,
            amount,
            "PROCESSING",
            LocalDateTime.now()
        );
        payments.put(id, payment);
        return payment;
    }

    public Optional<Payment> getPaymentById(Long id) {
        return Optional.ofNullable(payments.get(id));
    }
}

