package com.example.usermanagementmodule.model;
public class UserComment {
    private String userName;
    private String userPhotoUrl;
    private String bookName;
    private String bookCoverUrl;
    private String bookStatus; // "read" or"want to read" or "not read"
    private String commentText;
    private int userRating;


    public  UserComment(String userName, String userPhotoUrl, String bookName, String bookCoverUrl, String bookStatus, String commentText, int userRating) {
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.bookName = bookName;
        this.bookCoverUrl = bookCoverUrl;
        this.bookStatus = bookStatus;
        this.commentText = commentText;
        this.userRating = userRating;
    }


    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhotoUrl() { return userPhotoUrl; }
    public void setUserPhotoUrl(String userPhotoUrl) { this.userPhotoUrl = userPhotoUrl; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getBookCoverUrl() { return bookCoverUrl; }
    public void setBookCoverUrl(String bookCoverUrl) { this.bookCoverUrl = bookCoverUrl; }

    public String getBookStatus() { return bookStatus; }
    public void setBookStatus(String bookStatus) { this.bookStatus = bookStatus; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public int getUserRating() { return userRating; }
    public void setUserRating(int userRating) { this.userRating = userRating; }

    @Override
    public String toString() {
        return "UserProfileComment{" +
                "bookCoverUrl='" + bookCoverUrl + '\'' +
                ", userName='" + userName + '\'' +
                ", userPhotoUrl='" + userPhotoUrl + '\'' +
                ", bookName='" + bookName + '\'' +
                ", bookStatus='" + bookStatus + '\'' +
                ", commentText='" + commentText + '\'' +
                ", userRating=" + userRating +
                '}';
    }
}