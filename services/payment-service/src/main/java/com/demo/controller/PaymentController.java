package com.demo.controller;

import com.demo.dto.PaymentRequest;
import com.demo.dto.VersionResponse;
import com.demo.model.Payment;
import com.demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Value("${app.version}")
    private String version;

    @Value("${app.build.date}")
    private String buildDate;

    @Value("${app.build.commit}")
    private String buildCommit;

    @PostMapping("/payments")
    public ResponseEntity<Payment> createPayment(@RequestBody PaymentRequest request) {
        Payment payment = paymentService.createPayment(
            request.getBookingId(),
            request.getAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
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

