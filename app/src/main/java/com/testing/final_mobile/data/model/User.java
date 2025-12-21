package com.testing.final_mobile.data.model;

public class User {
    public String uid;
    public String name;
    public String profileImageUrl;
    public String fcmToken;

    public User() {}
    public User(String uid, String name, String profileImageUrl, String fcmToken) {
        this.uid = uid;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.fcmToken = fcmToken;
    }
    // Getters and Setters
}
