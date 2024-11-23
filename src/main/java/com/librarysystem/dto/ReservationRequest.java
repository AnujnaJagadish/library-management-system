package com.librarysystem.dto;

public class ReservationRequest {
    private Long bookId;
    private Long memberId;

    // Getters and setters
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
