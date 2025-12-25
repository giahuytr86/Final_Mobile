package com.testing.final_mobile.data.remote;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.ArrayList;
import java.util.List;

public class CommentRemoteDataSource {

    private static final String TAG = "CommentRemoteDS";
    private static final String POST_COLLECTION = "posts";
    private static final String COMMENT_SUB_COLLECTION = "comments";

    private final FirestoreService firestoreService;

    public interface OnCommentsFetchedListener {
        void onCommentsFetched(List<Comment> comments);
        void onError(Exception e);
    }

    public interface OnCommentAddedListener {
        void onCommentAdded();
        void onError(Exception e);
    }

    public CommentRemoteDataSource(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public void fetchCommentsForPost(String postId, OnCommentsFetchedListener listener) {
        if (postId == null || postId.trim().isEmpty()) {
            Log.e(TAG, "postId is null or empty!");
            listener.onCommentsFetched(new ArrayList<>());
            return;
        }

        String commentPath = POST_COLLECTION + "/" + postId + "/" + COMMENT_SUB_COLLECTION;
        Query query = firestoreService.getCollection(commentPath).orderBy("timestamp", Query.Direction.ASCENDING);

        firestoreService.getCollection(query, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Comment> comments = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Comment comment = doc.toObject(Comment.class);
                    comment.setId(doc.getId());
                    comment.setPostId(postId);
                    comments.add(comment);
                }
                listener.onCommentsFetched(comments);
            } else {
                Log.e(TAG, "Error fetching comments for post: " + postId, task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
    }

    public void addComment(Comment newComment, OnCommentAddedListener listener) {
        if (newComment.getPostId() == null || newComment.getPostId().isEmpty()) {
            listener.onError(new IllegalArgumentException("Post ID cannot be null or empty."));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection(POST_COLLECTION).document(newComment.getPostId());
        DocumentReference commentRef = postRef.collection(COMMENT_SUB_COLLECTION).document();

        // Sử dụng Transaction để đảm bảo việc thêm comment và tăng count diễn ra đồng thời
        db.runTransaction(transaction -> {
            // 1. Thêm comment mới
            transaction.set(commentRef, newComment);
            
            // 2. Tăng trường commentCount trong bài viết
            // Lưu ý: Đảm bảo field trong Firestore của bạn là 'commentCount'
            transaction.update(postRef, "commentCount", FieldValue.increment(1));
            
            return null;
        }).addOnSuccessListener(aVoid -> {
            listener.onCommentAdded();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding comment with transaction", e);
            listener.onError(e);
        });
    }
}
