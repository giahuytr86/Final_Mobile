package com.testing.final_mobile.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "posts")
public class Post {
    @PrimaryKey
    @NonNull
    private String id = ""; // Initialized to prevent null issues

    private String userId;
    private String username; // Corrected
    private String avatarUrl; // Corrected
    private String content;
    private String imageUrl;
    @ServerTimestamp
    private Date timestamp;
    private List<String> likes;
    private int commentCount;
    private String searchableContent;

    public Post() {
        // Default constructor required for Room and Firestore
    }

    @Ignore
    public Post(String id, String userId, String username, String avatarUrl, String content, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.content = content;
        this.imageUrl = imageUrl;
        this.timestamp = new Date();
        this.likes = new ArrayList<>();
        this.commentCount = 0;
        if (content != null) {
            this.searchableContent = content.toLowerCase();
        }
    }

    //<editor-fold desc="Getters and Setters">
    @NonNull
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; } // Corrected
    public String getAvatarUrl() { return avatarUrl; } // Corrected
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public Date getTimestamp() { return timestamp; }
    public List<String> getLikes() { return likes != null ? likes : new ArrayList<>(); }
    public int getCommentCount() { return commentCount; }
    public String getSearchableContent() { return searchableContent; }

    public void setId(@NonNull String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; } // Corrected
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; } // Corrected
    public void setContent(String content) {
        this.content = content;
        if (content != null) {
            this.searchableContent = content.toLowerCase();
        }
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setLikes(List<String> likes) { this.likes = likes; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public void setSearchableContent(String searchableContent) { this.searchableContent = searchableContent; }
    //</editor-fold>

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return commentCount == post.commentCount &&
                id.equals(post.id) &&
                Objects.equals(userId, post.userId) &&
                Objects.equals(content, post.content) &&
                Objects.equals(imageUrl, post.imageUrl) &&
                Objects.equals(likes, post.likes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, content, imageUrl, likes, commentCount);
    }
}
