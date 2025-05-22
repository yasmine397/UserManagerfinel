package com.example.usermanagementmodule.model;

import com.example.usermanagementmodule.book.Book;

import java.util.ArrayList;

public class User {

    private String username;
    private String email;
    private String phoneNumber;
    private String dateOfBirth;
    private String password;
    private String imageUrl;
    private ArrayList<Book> books;
    private static ArrayList<Comment> comments;
    private String bio;

    public User(String username, String email, String phoneNumber, String dateOfBirth, String password, String imageUrl) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
        this.imageUrl = imageUrl;
        this.books = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.bio="";
    }

    public User(String username, String imageUrl) {
        this.username = username;
        this.imageUrl = imageUrl;
    }

    public User() {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
        this.imageUrl = imageUrl;
        this.books = new ArrayList<>();
        this.comments = new ArrayList<>();
    }


    // Getters
    public  String getUsername() {
        return username;
    }

    public String getBio() {
        return bio;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPassword() {
        return password;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    public int getBooksReadCount() {
        return books.size();
    }

    public static ArrayList<Comment> getComments() {
        return comments;
    }
//setter

    public void setBooks(ArrayList<Book> books) {
        this.books = books;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    //for adding book and comment
    public void addBook(Book book) {
        books.add(book);
    }

    public void addComment(Comment comment) {
        getComments().add(comment);
    }

    @Override
    public String toString() {
        return "User{" +
                "bio='" + bio + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", password='" + password + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", books=" + books +
                '}';
    }

}

