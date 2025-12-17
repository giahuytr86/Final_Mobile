package com.testing.final_mobile.data.model;

public class CommentLike {

    private String commentId;
    private String userId;
    private long likedAt;


    public CommentLike() {}

    public CommentLike(String commentId, String userId) {
        this.commentId = commentId;
        this.userId = userId;
        this.likedAt = System.currentTimeMillis();
    }

    public String getCommentId() {
        return commentId;
    }

    public String getUserId() {
        return userId;
    }

    public long getLikedAt() {
        return likedAt;
    }
}
