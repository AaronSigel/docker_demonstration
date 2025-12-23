package com.demo.service;

import com.demo.model.Booking;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookingService {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Booking createBooking(Long workspaceId, String userId) {
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
        return booking;
    }

    public Optional<Booking> getBookingById(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }
}

