package com.demo.model;

import java.time.LocalDateTime;

public class Payment {
    private Long id;
    private Long bookingId;
    private Double amount;
    private String status;
    private LocalDateTime createdAt;

    public Payment() {
    }

    public Payment(Long id, Long bookingId, Double amount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

