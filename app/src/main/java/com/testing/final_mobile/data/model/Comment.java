package com.testing.final_mobile.data.model;

public class Comment {

    private String commentId;
    private String postId;
    private String userId;
    private String userName;
    private String content;
    private long createdAt;
    private int likeCount;
    private int replyCount;


    public Comment() {}

    public Comment(String commentId,
                   String postId,
                   String userId,
                   String userName,
                   String content,
                   long createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
        this.likeCount = 0;
        this.replyCount = 0;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getPostId() {
        return postId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void increaseLike() {
        likeCount++;
    }

    public void decreaseLike() {
        if (likeCount > 0) likeCount--;
    }

    public void increaseReply() {
        replyCount++;
    }
}
