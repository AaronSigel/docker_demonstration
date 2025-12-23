package com.demo.model;

import java.time.LocalDateTime;

public class Booking {
    private Long id;
    private Long workspaceId;
    private String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public Booking() {
    }

    public Booking(Long id, Long workspaceId, String userId, LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

