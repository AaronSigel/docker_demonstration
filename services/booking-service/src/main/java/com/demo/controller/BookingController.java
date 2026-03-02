package com.demo.controller;

import com.demo.dto.BookingRequest;
import com.demo.dto.VersionResponse;
import com.demo.model.Booking;
import com.demo.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Value("${app.version}")
    private String version;

    @Value("${app.build.date}")
    private String buildDate;

    @Value("${app.build.commit}")
    private String buildCommit;

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        logger.info("Создание бронирования (POST /api/bookings): workspaceId={}, userId={}",
                request.getWorkspaceId(), request.getUserId());
        try {
            Booking booking = bookingService.createBooking(
                request.getWorkspaceId(),
                request.getUserId()
            );
            logger.info("Создано бронирование с id={}", booking.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (RuntimeException e) {
            logger.error("Ошибка при создании бронирования: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ошибка при создании бронирования: " + e.getMessage());
        }
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        logger.info("Получение бронирования (GET /api/bookings/{} )", id);
        return bookingService.getBookingById(id)
            .map(booking -> {
                logger.info("Бронирование с id={} найдено", id);
                return ResponseEntity.ok(booking);
            })
            .orElseGet(() -> {
                logger.info("Бронирование с id={} не найдено", id);
                return ResponseEntity.notFound().build();
            });
    }

    @GetMapping("/version")
    public ResponseEntity<VersionResponse> getVersion() {
        VersionResponse response = new VersionResponse(
            version,
            buildDate,
            buildCommit,
            "booking-service"
        );
        return ResponseEntity.ok(response);
    }
}

