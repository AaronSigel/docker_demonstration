package com.demo.service;

import com.demo.model.Payment;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

@Service
public class PaymentService {
    private final Map<Long, Payment> payments = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Random random = new Random();
    private final Counter paymentCreatedCounter;
    private final Counter paymentErrorCounter;
    private final Counter paymentHighAmountCounter;
    private final Timer paymentProcessingTimer;

    public PaymentService(MeterRegistry meterRegistry) {
        this.paymentCreatedCounter = Counter.builder("payment.created")
                .description("Количество созданных платежей")
                .register(meterRegistry);
        this.paymentErrorCounter = Counter.builder("payment.errors")
                .description("Количество ошибок при обработке платежей")
                .tag("type", "processing")
                .register(meterRegistry);
        this.paymentHighAmountCounter = Counter.builder("payment.high.amount")
                .description("Количество платежей с высокой суммой (>500)")
                .register(meterRegistry);
        this.paymentProcessingTimer = Timer.builder("payment.processing.time")
                .description("Время обработки платежа")
                .register(meterRegistry);
    }

    public Payment createPayment(Long bookingId, Double amount) {
        try {
            return paymentProcessingTimer.recordCallable(() -> {
            try {
                // Демонстрация: большие суммы обрабатываются дольше
                int baseDelay = 100;
                int additionalDelay = amount > 500 ? 300 + random.nextInt(400) : random.nextInt(200);
                Thread.sleep(baseDelay + additionalDelay);

                // Демонстрация: 3% вероятность ошибки для обычных платежей, 10% для больших сумм
                int errorChance = amount > 500 ? 10 : 3;
                if (random.nextInt(100) < errorChance) {
                    paymentErrorCounter.increment();
                    throw new RuntimeException("Ошибка при обработке платежа (демонстрация)");
                }

                if (amount > 500) {
                    paymentHighAmountCounter.increment();
                }

                Long id = idGenerator.getAndIncrement();
                Payment payment = new Payment(
                    id,
                    bookingId,
                    amount,
                    "PROCESSING",
                    LocalDateTime.now()
                );
                payments.put(id, payment);
                paymentCreatedCounter.increment();
                return payment;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                paymentErrorCounter.increment();
                throw new RuntimeException("Прервана обработка платежа", e);
            }
        });
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Ошибка при создании платежа", e);
        }
    }

    public Optional<Payment> getPaymentById(Long id) {
        return Optional.ofNullable(payments.get(id));
    }
}

