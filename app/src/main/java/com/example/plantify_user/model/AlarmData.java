package com.example.plantify_user.model;

public class AlarmData {
    private String id;
    private String name;
    private String time;
    private String imageUrl;
    private boolean isActive;

    public AlarmData() {
        // Required empty constructor for Firebase
    }

    public AlarmData(String id, String name, String time, String imageUrl) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.imageUrl = imageUrl;
        this.isActive = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}