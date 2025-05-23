package com.example.usermanagementmodule.model;
public class UserComment {
    private String userName;
    private String userPhotoUrl;
    private String bookName;
    private String bookCoverUrl;
    private String bookStatus; // "read" or"want to read" or "not read"
    private String commentText;
    private int userRating;
    private String commentId;
    private String userId;

    public UserComment(String userName, String userPhotoUrl, String bookName, String bookCoverUrl, String bookStatus, String commentText, int userRating) { // Add userId to constructor
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.bookName = bookName;
        this.bookCoverUrl = bookCoverUrl;
        this.bookStatus = bookStatus;
        this.commentText = commentText;
        this.userRating = userRating;
        this.userId = userId;
    }
    public UserComment() {}

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

    public String getCommentId() { return commentId; } // Add getter for commentId
    public void setCommentId(String commentId) { this.commentId = commentId; } // Add setter for commentId

    public String getUserId() { return userId; } // Add getter for userId
    public void setUserId(String userId) { this.userId = userId; } // Add setter for userId

    @Override
    public String toString() {
        return "UserProfileComment{" +
                "commentId='" + commentId + '\'' + // Include commentId in toString
                "userId='" + userId + '\'' +       // Include userId in toString
                ", bookCoverUrl='" + bookCoverUrl + '\'' +
                ", userName='" + userName + '\'' +
                ", userPhotoUrl='" + userPhotoUrl + '\'' +
                ", bookName='" + bookName + '\'' +
                ", bookStatus='" + bookStatus + '\'' +
                ", commentText='" + commentText + '\'' +
                ", userRating=" + userRating +
                '}';
    }
}