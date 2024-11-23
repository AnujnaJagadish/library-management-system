package com.librarysystem.dto;

import java.time.LocalDateTime;

/**
 * DTO for transferring notification details.
 */
public class NotificationRequestDTO {

    private Long notificationId;
    private Long memberId;
    private String message;
    private boolean isRead;
    private Long reservationId;
    private LocalDateTime returnedDate;

    // Constructor for full initialization
    public NotificationRequestDTO(Long notificationId, Long memberId, String message, boolean isRead, Long reservationId, LocalDateTime returnedDate) {
        this.notificationId = notificationId;
        this.memberId = memberId;
        this.message = message;
        this.isRead = isRead;
        this.reservationId = reservationId;
        this.returnedDate = returnedDate; 
    }

    // Constructor for notifications without memberId
    public NotificationRequestDTO(Long notificationId, String message, boolean isRead) {
        this.notificationId = notificationId; 
        this.message = message;
        this.isRead = isRead;
    }

    // Constructor for notifications without notificationId
    public NotificationRequestDTO(String message, boolean isRead) {
        this.message = message;
        this.isRead = isRead;
    }

    // Default Constructor
    public NotificationRequestDTO() {
    }

    // Getters and Setters
    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public LocalDateTime getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDateTime returnedDate) {
        this.returnedDate = returnedDate;
    }
}
