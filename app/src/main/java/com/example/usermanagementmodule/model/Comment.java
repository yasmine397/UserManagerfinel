package com.example.usermanagementmodule.model;

public class Comment {

    private String commentText;
    private User user;

    public Comment(User user,String commentText)
    {
        this.user=user;
        this.commentText=commentText;
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

    @Override
    public String toString() {
        return "Comment{" +
                "commentText='" + commentText + '\'' +
                ", user=" + user +
                '}';
    }
}
