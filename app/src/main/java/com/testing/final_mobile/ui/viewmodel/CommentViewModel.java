package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.data.repository.CommentRepository;

import java.util.List;

public class CommentViewModel extends AndroidViewModel {

    private static final String TAG = "CommentViewModel";

    private final CommentRepository commentRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _commentAdded = new MutableLiveData<>(false);
    public LiveData<Boolean> commentAdded = _commentAdded;

    public CommentViewModel(@NonNull Application application) {
        super(application);
        this.commentRepository = new CommentRepository(application);
    }

    public void toggleLikeStatus(String postId, String commentId) {
        commentRepository.toggleLikeStatus(postId, commentId, new CommentRepository.OnCommentLikedListener() {
            @Override
            public void onCommentLiked() {
                Log.d(TAG, "Like status toggled for comment: " + commentId);
            }

            @Override
            public void onError(Exception e) {
                _error.setValue(e.getMessage());
            }
        });
    }

    public LiveData<List<Comment>> getCommentsForPost(String postId) {
        return commentRepository.getCommentsForPost(postId);
    }

    public void addComment(String postId, String content, String parentCommentId) {
        _isLoading.setValue(true);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            _error.setValue("User not logged in.");
            _isLoading.setValue(false);
            return;
        }

        String userId = firebaseUser.getUid();
        String userName = firebaseUser.getDisplayName();
        String userAvatar = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";

        Comment newComment = new Comment();
        newComment.setPostId(postId);
        newComment.setUserId(userId);
        newComment.setUserName(userName);
        newComment.setUserAvatarUrl(userAvatar);
        newComment.setContent(content);
        newComment.setParentCommentId(parentCommentId);

        commentRepository.addComment(newComment, new CommentRepository.OnCommentAddedListener() {
            @Override
            public void onCommentAdded() {
                _isLoading.setValue(false);
                _commentAdded.setValue(true);
                _commentAdded.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _error.setValue(e.getMessage());
            }
        });
    }
}
