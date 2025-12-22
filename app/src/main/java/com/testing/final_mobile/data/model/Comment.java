package com.testing.final_mobile.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@Entity(tableName = "comments",
        foreignKeys = @ForeignKey(entity = Post.class,
                parentColumns = "id",
                childColumns = "postId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("postId")})
public class Comment {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String postId;
    private String userId;
    private String username;
    private String avatarUrl;
    private String content;

    @ServerTimestamp
    private Date timestamp;

    private String parentCommentId;

    // 'likes' field is removed

    public Comment() {
        // Default constructor
    }

    //<editor-fold desc="Getters and Setters">
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }
    //</editor-fold>
}
