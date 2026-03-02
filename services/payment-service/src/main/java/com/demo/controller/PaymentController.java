package com.demo.controller;

import com.demo.dto.PaymentRequest;
import com.demo.dto.VersionResponse;
import com.demo.model.Payment;
import com.demo.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Value("${app.version}")
    private String version;

    @Value("${app.build.date}")
    private String buildDate;

    @Value("${app.build.commit}")
    private String buildCommit;

    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
        logger.info("Создание платежа (POST /api/payments): bookingId={}, amount={}",
                request.getBookingId(), request.getAmount());
        try {
            Payment payment = paymentService.createPayment(
                request.getBookingId(),
                request.getAmount()
            );
            logger.info("Создан платеж с id={}", payment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (RuntimeException e) {
            logger.error("Ошибка при обработке платежа: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ошибка при обработке платежа: " + e.getMessage());
        }
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
        logger.info("Получение платежа (GET /api/payments/{})", id);
        return paymentService.getPaymentById(id)
            .map(payment -> {
                logger.info("Платеж с id={} найден", id);
                return ResponseEntity.ok(payment);
            })
            .orElseGet(() -> {
                logger.info("Платеж с id={} не найден", id);
                return ResponseEntity.notFound().build();
            });
    }

    @GetMapping("/version")
    public ResponseEntity<VersionResponse> getVersion() {
        VersionResponse response = new VersionResponse(
            version,
            buildDate,
            buildCommit,
            "payment-service"
        );
        return ResponseEntity.ok(response);
    }
}

