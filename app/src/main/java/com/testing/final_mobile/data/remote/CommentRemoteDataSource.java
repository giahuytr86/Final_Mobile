package com.testing.final_mobile.data.remote;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommentRemoteDataSource {

    private static final String TAG = "CommentRemoteDS";
    private static final String POST_COLLECTION = "posts";
    private static final String COMMENT_SUB_COLLECTION = "comments";

    private final FirestoreService firestoreService;

    //<editor-fold desc="Interfaces">
    public interface OnCommentsFetchedListener {
        void onCommentsFetched(List<Comment> comments);
        void onError(Exception e);
    }

    public interface OnCommentAddedListener {
        void onCommentAdded(DocumentReference documentReference);
        void onError(Exception e);
    }

    public interface OnCommentLikeUpdatedListener {
        void onCommentLikeUpdated();
        void onError(Exception e);
    }
    //</editor-fold>

    public CommentRemoteDataSource(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public void toggleLikeStatus(String postId, String commentId, String userId, OnCommentLikeUpdatedListener listener) {
        String commentPath = POST_COLLECTION + "/" + postId + "/" + COMMENT_SUB_COLLECTION;
        DocumentReference commentRef = firestoreService.getDocument(commentPath, commentId);

        Transaction.Function<Void> updateFunction = transaction -> {
            Comment comment = transaction.get(commentRef).toObject(Comment.class);
            if (comment == null) {
                throw new FirebaseFirestoreException("Comment not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            Map<String, Boolean> likes = comment.getLikes();
            if (likes.containsKey(userId)) {
                likes.remove(userId);
                comment.setLikeCount(comment.getLikeCount() - 1);
            } else {
                likes.put(userId, true);
                comment.setLikeCount(comment.getLikeCount() + 1);
            }
            transaction.set(commentRef, comment);
            return null;
        };

        firestoreService.runTransaction(updateFunction, (OnCompleteListener<Void>) task -> {
            if (task.isSuccessful()) {
                listener.onCommentLikeUpdated();
            } else {
                Log.e(TAG, "Like transaction failed for comment", task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
    }

    public void fetchCommentsForPost(String postId, OnCommentsFetchedListener listener) {
        String commentPath = POST_COLLECTION + "/" + postId + "/" + COMMENT_SUB_COLLECTION;
        Query query = firestoreService.getCollection(commentPath).orderBy("createdAt", Query.Direction.ASCENDING);

        firestoreService.getCollection(query, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Comment> comments = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Comment comment = doc.toObject(Comment.class);
                    comment.setCommentId(doc.getId());
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
        String commentPath = POST_COLLECTION + "/" + newComment.getPostId() + "/" + COMMENT_SUB_COLLECTION;
        firestoreService.addDocument(commentPath, newComment, task -> {
            if (task.isSuccessful()) {
                listener.onCommentAdded(task.getResult());
            } else {
                Log.e(TAG, "Error adding comment", task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
    }
}
