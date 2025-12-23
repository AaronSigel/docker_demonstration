package com.demo.controller;

import com.demo.dto.BookingRequest;
import com.demo.dto.VersionResponse;
import com.demo.model.Booking;
import com.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Value("${app.version}")
    private String version;

    @Value("${app.build.date}")
    private String buildDate;

    @Value("${app.build.commit}")
    private String buildCommit;

    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(
            request.getWorkspaceId(),
            request.getUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        return bookingService.getBookingById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
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

