package com.librarysystem.dto;

public class BookDTO {
    private String title;
    private String author;
    private String genre;
    private int count;

    // Default no-argument constructor (required by Jackson)
    public BookDTO() {
    }

    public BookDTO(String title, String author, String genre, int count) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.count = count;
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
}
