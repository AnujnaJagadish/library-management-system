package com.librarysystem.dto;

public class BorrowHistoryDTO {
    private String memberName;
    private String bookTitle;
    private String author;
    private String actionType;
    private String date;

    public BorrowHistoryDTO(String memberName, String bookTitle, String author, String actionType, String date) {
        this.memberName = memberName;
        this.bookTitle = bookTitle;
        this.author = author;
        this.actionType = actionType;
        this.date = date;
    }

    // Getters and Setters
    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
