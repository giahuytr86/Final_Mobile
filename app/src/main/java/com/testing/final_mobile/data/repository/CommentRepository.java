package com.testing.final_mobile.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.testing.final_mobile.data.local.AppDatabase;
import com.testing.final_mobile.data.local.CommentDao;
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.remote.CommentRemoteDataSource;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.List;

public class CommentRepository {

    private static final String TAG = "CommentRepository";

    private final CommentDao commentDao;
    private final CommentRemoteDataSource remoteDataSource;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public interface OnCommentOperationListener {
        void onSuccess();
        void onError(Exception e);
    }

    public CommentRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.commentDao = database.commentDao();
        this.remoteDataSource = new CommentRemoteDataSource(new FirestoreService());
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public LiveData<List<Comment>> getCommentsForPost(String postId) {
        refreshCommentsFromServer(postId);
        return commentDao.getCommentsForPost(postId);
    }

    public void addComment(String postId, String content, String parentCommentId, OnCommentOperationListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        firestore.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    String username = (user != null && user.getUsername() != null) ? user.getUsername() : currentUser.getEmail();
                    String avatarUrl = (user != null) ? user.getAvatarUrl() : null;

                    Comment newComment = new Comment();
                    newComment.setPostId(postId);
                    newComment.setUserId(currentUser.getUid());
                    newComment.setUsername(username);
                    newComment.setAvatarUrl(avatarUrl);
                    newComment.setContent(content);
                    newComment.setParentCommentId(parentCommentId);

                    remoteDataSource.addComment(newComment, new CommentRemoteDataSource.OnCommentAddedListener() {
                        @Override
                        public void onCommentAdded(DocumentReference documentReference) {
                            // Tăng số lượng commentCount trong document của Post tương ứng
                            firestore.collection("posts").document(postId)
                                    .update("commentCount", FieldValue.increment(1))
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Comment count incremented"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error incrementing comment count", e));

                            refreshCommentsFromServer(postId);
                            listener.onSuccess();
                        }

                        @Override
                        public void onError(Exception e) {
                            listener.onError(e);
                        }
                    });
                })
                .addOnFailureListener(listener::onError);
    }

    private void refreshCommentsFromServer(String postId) {
        remoteDataSource.fetchCommentsForPost(postId, new CommentRemoteDataSource.OnCommentsFetchedListener() {
            @Override
            public void onCommentsFetched(List<Comment> comments) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    commentDao.deleteAll(); // Simple cache invalidation
                    commentDao.insertAll(comments);
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing comments from server for post: " + postId, e);
            }
        });
    }
}
