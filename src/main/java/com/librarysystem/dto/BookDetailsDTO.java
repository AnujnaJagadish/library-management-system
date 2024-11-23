package com.librarysystem.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for book details, including borrowed members and reviews.
 */
public class BookDetailsDTO {

    private Long id;
    private String title;
    private String author;
    private String genre;
    private int count;
    private int reservedCount;
    private int borrowedCount;
    private List<BorrowedMemberDTO> borrowedMembers;
    private List<ReviewDTO> reviews;

    // All-argument constructor
    public BookDetailsDTO(Long id, String title, String author, String genre, int count, int reservedCount, int borrowedCount,
                          List<BorrowedMemberDTO> borrowedMembers, List<ReviewDTO> reviews) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.count = count;
        this.reservedCount = reservedCount;
        this.borrowedCount = borrowedCount;
        this.borrowedMembers = borrowedMembers;
        this.reviews = reviews;
    }

    // Default no-argument constructor
    public BookDetailsDTO() {
    }

    // Getters and setters for all fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getReservedCount() {
        return reservedCount;
    }

    public void setReservedCount(int reservedCount) {
        this.reservedCount = reservedCount;
    }

    public int getBorrowedCount() {
        return borrowedCount;
    }

    public void setBorrowedCount(int borrowedCount) {
        this.borrowedCount = borrowedCount;
    }

    public List<BorrowedMemberDTO> getBorrowedMembers() {
        return borrowedMembers;
    }

    public void setBorrowedMembers(List<BorrowedMemberDTO> borrowedMembers) {
        this.borrowedMembers = borrowedMembers;
    }

    public List<ReviewDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    // Nested DTO class for BorrowedMember
    public static class BorrowedMemberDTO {
        private String name; 
        private LocalDateTime borrowedDate;
        private LocalDateTime dueDate;

        public BorrowedMemberDTO(String name, LocalDateTime borrowedDate, LocalDateTime dueDate) {
            this.name = name;
            this.borrowedDate = borrowedDate;
            this.dueDate = dueDate;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDateTime getBorrowedDate() {
            return borrowedDate;
        }

        public void setBorrowedDate(LocalDateTime borrowedDate) {
            this.borrowedDate = borrowedDate;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
        }
    }

    public static class ReviewDTO {
        private String memberName;
        private int rating;
        private String reviewText;
        private LocalDateTime reviewDate;

        public ReviewDTO() {
        }

        public ReviewDTO(String memberName, int rating, String reviewText, LocalDateTime reviewDate) {
            this.memberName = memberName;
            this.rating = rating;
            this.reviewText = reviewText;
            this.reviewDate = reviewDate;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public String getReviewText() {
            return reviewText;
        }

        public void setReviewText(String reviewText) {
            this.reviewText = reviewText;
        }

        public LocalDateTime getReviewDate() {
            return reviewDate;
        }

        public void setReviewDate(LocalDateTime reviewDate) {
            this.reviewDate = reviewDate;
        }
    }
    
}
