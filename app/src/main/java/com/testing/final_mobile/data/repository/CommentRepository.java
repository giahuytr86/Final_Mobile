package com.testing.final_mobile.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.testing.final_mobile.data.local.AppDatabase;
import com.testing.final_mobile.data.local.CommentDao;
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.data.remote.CommentRemoteDataSource;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.List;

public class CommentRepository {

    private static final String TAG = "CommentRepository";

    private final CommentDao commentDao;
    private final CommentRemoteDataSource remoteDataSource;

    //<editor-fold desc="Interfaces">
    public interface OnCommentAddedListener {
        void onCommentAdded();
        void onError(Exception e);
    }

    public interface OnCommentLikedListener {
        void onCommentLiked();
        void onError(Exception e);
    }
    //</editor-fold>

    public CommentRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.commentDao = database.commentDao();
        this.remoteDataSource = new CommentRemoteDataSource(new FirestoreService());
    }

    public void toggleLikeStatus(String postId, String commentId, OnCommentLikedListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        remoteDataSource.toggleLikeStatus(postId, commentId, currentUserId, new CommentRemoteDataSource.OnCommentLikeUpdatedListener() {
            @Override
            public void onCommentLikeUpdated() {
                // After updating on the server, refresh the local data
                refreshCommentsFromServer(postId);
                listener.onCommentLiked();
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public LiveData<List<Comment>> getCommentsForPost(String postId) {
        refreshCommentsFromServer(postId);
        return commentDao.getCommentsForPost(postId);
    }

    public void addComment(Comment newComment, OnCommentAddedListener listener) {
        remoteDataSource.addComment(newComment, new CommentRemoteDataSource.OnCommentAddedListener() {
            @Override
            public void onCommentAdded(DocumentReference documentReference) {
                refreshCommentsFromServer(newComment.getPostId());
                listener.onCommentAdded();
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    private void refreshCommentsFromServer(String postId) {
        remoteDataSource.fetchCommentsForPost(postId, new CommentRemoteDataSource.OnCommentsFetchedListener() {
            @Override
            public void onCommentsFetched(List<Comment> comments) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    commentDao.insertAll(comments);
                    Log.d(TAG, "Refreshed " + comments.size() + " comments for post " + postId);
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching comments for post " + postId, e);
            }
        });
    }
}
