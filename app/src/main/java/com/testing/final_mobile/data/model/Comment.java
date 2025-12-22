package com.testing.final_mobile.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import com.testing.final_mobile.data.local.converters.DateConverter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity(tableName = "comments",
        foreignKeys = @ForeignKey(entity = Post.class,
                                  parentColumns = "postId",
                                  childColumns = "postId",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index("postId")})
@TypeConverters(DateConverter.class)
public class Comment {

    @PrimaryKey
    @NonNull
    private String commentId;

    private String postId;
    private String userId;
    private String userName;
    private String userAvatarUrl; // Added for UI
    private String content;

    @ServerTimestamp
    private Date createdAt;

    private int likeCount = 0;
    private int replyCount = 0;

    private String parentCommentId; // Added for replies

    // Map to store user IDs of who liked the comment
    private Map<String, Boolean> likes = new HashMap<>();

    public Comment() {
        this.commentId = ""; // Ensure non-null
    }

    //<editor-fold desc="Getters and Setters">
    @NonNull
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(@NonNull String commentId) {
        this.commentId = commentId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
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
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
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
