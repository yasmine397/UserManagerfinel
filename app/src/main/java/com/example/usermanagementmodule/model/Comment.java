package com.example.usermanagementmodule.model;

public class Comment {

    private String commentText;
    private User user;
    private String bookId;
    private String userName;
    private String userId;
    private String userPhotoUrl;

    // Existing constructor
    public Comment(User user,String commentText)
    {
        this.user=user;
        this.commentText=commentText;
    }

    // Add a default constructor (often needed for Firestore deserialization)
    public Comment() {
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public  User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Add getters and setters for the new fields
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }


    @Override
    public String toString() {
        return "Comment{" +
                "commentText='" + commentText + '\'' +
                ", user=" + user +
                ", bookId='" + bookId + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userPhotoUrl='" + userPhotoUrl + '\'' +
                '}';
    }
}
