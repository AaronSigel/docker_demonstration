package com.demo.service;

import com.demo.model.Booking;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

@Service
public class BookingService {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Random random = new Random();
    private final Counter bookingCreatedCounter;
    private final Counter bookingErrorCounter;
    private final Timer bookingProcessingTimer;

    public BookingService(MeterRegistry meterRegistry) {
        this.bookingCreatedCounter = Counter.builder("booking.created")
                .description("Количество созданных бронирований")
                .register(meterRegistry);
        this.bookingErrorCounter = Counter.builder("booking.errors")
                .description("Количество ошибок при создании бронирований")
                .tag("type", "creation")
                .register(meterRegistry);
        this.bookingProcessingTimer = Timer.builder("booking.processing.time")
                .description("Время обработки бронирования")
                .register(meterRegistry);
    }

    public Booking createBooking(Long workspaceId, String userId) {
        try {
            return bookingProcessingTimer.recordCallable(() -> {
            try {
                // Демонстрация: случайная задержка 50-500ms
                int delay = 50 + random.nextInt(450);
                Thread.sleep(delay);

                // Демонстрация: 5% вероятность ошибки
                if (random.nextInt(100) < 5) {
                    bookingErrorCounter.increment();
                    throw new RuntimeException("Ошибка при создании бронирования (демонстрация)");
                }

                Long id = idGenerator.getAndIncrement();
                LocalDateTime now = LocalDateTime.now();
                Booking booking = new Booking(
                    id,
                    workspaceId,
                    userId,
                    now,
                    now.plusHours(2),
                    "ACTIVE"
                );
                bookings.put(id, booking);
                bookingCreatedCounter.increment();
                return booking;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                bookingErrorCounter.increment();
                throw new RuntimeException("Прервано создание бронирования", e);
            }
        });
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Ошибка при создании бронирования", e);
        }
    }

    public Optional<Booking> getBookingById(Long id) {
        // Демонстрация: задержка при получении несуществующего бронирования
        if (!bookings.containsKey(id)) {
            try {
                Thread.sleep(100 + random.nextInt(200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return Optional.ofNullable(bookings.get(id));
    }
}

