package com.testing.final_mobile.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import com.testing.final_mobile.data.local.converters.DateConverter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity(tableName = "posts")
@TypeConverters(DateConverter.class)
public class Post {

    @PrimaryKey
    @NonNull
    private String postId;

    private String userId;
    private String userName;
    private String userAvatarUrl;
    private String content;
    private String imageUrl;
    private String searchableContent; // For case-insensitive search

    @ServerTimestamp
    private Date timestamp;

    private int likeCount = 0;
    private int commentCount = 0;

    private Map<String, Boolean> likes = new HashMap<>();

    public Post() {
        this.postId = ""; // Ensure postId is not null
    }

    //<editor-fold desc="Getters and Setters">
    @NonNull
    public String getPostId() {
        return postId;
    }

    public void setPostId(@NonNull String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        if (content != null) {
            this.searchableContent = content.toLowerCase();
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSearchableContent() {
        return searchableContent;
    }

    public void setSearchableContent(String searchableContent) {
        this.searchableContent = searchableContent;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    @Exclude
    public boolean isLikedBy(String userId) {
        return likes.containsKey(userId);
    }
    //</editor-fold>
}
