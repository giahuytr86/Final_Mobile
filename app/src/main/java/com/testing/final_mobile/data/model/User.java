package com.testing.final_mobile.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    @NonNull
    private String uid;

    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private List<String> following = new ArrayList<>();
    private List<String> followers = new ArrayList<>();

    public User() {
        this.uid = ""; // Ensure non-null
    }

    public User(@NonNull String uid, String username, String email, String avatarUrl) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    //<editor-fold desc="Getters and Setters">
    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }
    //</editor-fold>
}
