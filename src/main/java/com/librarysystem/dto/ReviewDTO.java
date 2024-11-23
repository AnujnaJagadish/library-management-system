package com.librarysystem.dto;

public class ReviewDTO {
    private String memberName;
    private Integer rating;
    private String reviewText;
    private java.time.LocalDateTime reviewDate;

    public ReviewDTO() {
    }

    // Constructor
    public ReviewDTO(String memberName, Integer rating, String reviewText, java.time.LocalDateTime reviewDate) {
        this.memberName = memberName;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reviewDate = reviewDate;
    }

    // Getters and Setters
    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public java.time.LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(java.time.LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }
}
