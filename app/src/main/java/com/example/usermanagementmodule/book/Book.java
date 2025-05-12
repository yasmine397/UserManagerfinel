package com.example.usermanagementmodule.book;

public class Book {
    private String name;
    private String realestDate;
    private String deseridsion;
    private String booklan;
    private String photo;
    private String pdfUrl;
    private String status = "not read";
    private String bookId;


    // Empty constructor needed for Firestore
    public Book() {
        // Required empty constructor for Firestore
    }

    public Book(String name, String realestDate, String deseridsion, String booklan, String photo) {
        this.name = name;
        this.realestDate = realestDate;
        this.deseridsion = deseridsion;
        this.booklan = booklan;
        this.photo = photo;
    }

    public Book(String name, String realestDate, String deseridsion, String booklan, String photo, String pdfUrl) {
        this.name = name;
        this.realestDate = realestDate;
        this.deseridsion = deseridsion;
        this.booklan = booklan;
        this.photo = photo;
        this.pdfUrl = pdfUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealestDate() {
        return realestDate;
    }

    public void setRealestDate(String realestDate) {
        this.realestDate = realestDate;
    }

    public String getDeseridsion() {
        return deseridsion;
    }

    public void setDeseridsion(String deseridsion) {
        this.deseridsion = deseridsion;
    }

    public String getBooklan() {
        return booklan;
    }

    public void setBooklan(String booklan) {
        this.booklan = booklan;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", realestDate='" + realestDate + '\'' +
                ", deseridsion='" + deseridsion + '\'' +
                ", booklan='" + booklan + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }
}
