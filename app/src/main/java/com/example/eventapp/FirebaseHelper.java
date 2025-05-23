package com.example.eventapp;

public class FirebaseHelper {
    public static class EventDTO {
        private String title;
        private String date;
        private String location;
        private String description;
        private int capacity;
        private String imageUrl;

        public EventDTO() {}

        public EventDTO(String title, String date, String location, String description, int capacity, String imageUrl) {
            this.title = title;
            this.date = date;
            this.location = location;
            this.description = description;
            this.capacity = capacity;
            this.imageUrl = imageUrl;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}