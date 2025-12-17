package com.testing.final_mobile.data;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.testing.final_mobile.data.FirebaseManager;
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.data.model.CommentLike;

import java.util.HashMap;
import java.util.Map;

public class CommentRepository {

    private final FirebaseFirestore db;
    private final CollectionReference commentRef;
    private final CollectionReference commentLikeRef;

    public CommentRepository() {
        db = FirebaseManager.getFirestore();
        commentRef = db.collection("comments");
        commentLikeRef = db.collection("commentLikes");
    }

    /**
     * Get all comments of a post (including replies if parentId != null)
     */
    public ListenerRegistration getCommentsByPost(
            String postId,
            EventListener<QuerySnapshot> listener
    ) {
        return commentRef
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }

    /**
     * Add new root comment
     */
    public void addComment(String postId, String content) {
        String userId = FirebaseManager.getCurrentUserId();
        if (userId == null) return;

        String commentId = commentRef.document().getId();

        Comment comment = new Comment(
                commentId,
                postId,
                userId,
                "Anonymous", // có thể thay bằng displayName sau
                content,
                System.currentTimeMillis()
        );

        commentRef.document(commentId).set(comment);
    }

    /**
     * Reply to a comment
     */
    public void replyComment(String postId, String parentCommentId, String content) {
        String userId = FirebaseManager.getCurrentUserId();
        if (userId == null) return;

        String commentId = commentRef.document().getId();

        Comment reply = new Comment(
                commentId,
                postId,
                userId,
                "Anonymous",
                content,
                System.currentTimeMillis()
        );

        // đánh dấu là reply
        commentRef.document(commentId).set(reply);

        // tăng replyCount cho comment cha
        commentRef.document(parentCommentId)
                .update("replyCount", FieldValue.increment(1));
    }

    /**
     * Like / Unlike a comment (toggle)
     */
    public void toggleLikeComment(String commentId) {
        String userId = FirebaseManager.getCurrentUserId();
        if (userId == null) return;

        String likeDocId = commentId + "_" + userId;

        commentLikeRef.document(likeDocId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // UNLIKE
                        commentLikeRef.document(likeDocId).delete();
                        updateLikeCount(commentId, -1);
                    } else {
                        // LIKE
                        CommentLike like = new CommentLike(commentId, userId);
                        commentLikeRef.document(likeDocId).set(like);
                        updateLikeCount(commentId, +1);
                    }
                });
    }

    private void updateLikeCount(String commentId, int delta) {
        commentRef.document(commentId)
                .update("likeCount", FieldValue.increment(delta));
    }
}