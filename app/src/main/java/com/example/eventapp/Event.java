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

    public Event() {}

    public Event(String title, Date date, String location, String description, int capacity, String imageUrl) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.description = description;
        this.capacity = capacity;
        this.imageUrl = imageUrl;
    }

    // Add getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}