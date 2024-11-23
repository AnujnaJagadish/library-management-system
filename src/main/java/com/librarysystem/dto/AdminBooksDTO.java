package com.librarysystem.dto;

/**
 * DTO for administrative book management, including reserved and borrowed counts.
 */
public class AdminBooksDTO {
    private Long id; 
    private String title;
    private String author;
    private String genre;
    private int count;
    private int reservedCount;
    private int borrowedCount;

    // Default no-argument constructor (required by Jackson)
    public AdminBooksDTO() {
    }

    // All-argument constructor
    public AdminBooksDTO(Long id, String title, String author, String genre, int count, int reservedCount, int borrowedCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.count = count;
        this.reservedCount = reservedCount;
        this.borrowedCount = borrowedCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getters and Setters
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
}
