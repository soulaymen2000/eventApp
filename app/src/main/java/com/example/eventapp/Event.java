package com.example.eventapp;

import java.util.Date;

public class Event {
    private String id;
    private String title;
    private Date date;
    private String location;
    private String description;
    private int capacity;
    private String imageUrl;

    public Event() {
    }

    public Event(String title, Date date, String location, String description, int capacity, String imageUrl) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.description = description;
        this.capacity = capacity;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}