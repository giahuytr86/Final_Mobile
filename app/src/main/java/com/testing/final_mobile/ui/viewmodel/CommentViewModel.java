package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.data.repository.CommentRepository;

import java.util.List;

public class CommentViewModel extends AndroidViewModel {

    private final CommentRepository commentRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _commentAdded = new MutableLiveData<>(false);
    public final LiveData<Boolean> commentAdded = _commentAdded;

    public CommentViewModel(@NonNull Application application) {
        super(application);
        this.commentRepository = new CommentRepository(application);
    }

    public LiveData<List<Comment>> getCommentsForPost(String postId) {
        return commentRepository.getCommentsForPost(postId);
    }

    public void addComment(String postId, String content, String parentCommentId) {
        _isLoading.setValue(true);
        commentRepository.addComment(postId, content, parentCommentId, new CommentRepository.OnCommentOperationListener() {
            @Override
            public void onSuccess() {
                _isLoading.postValue(false);
                _commentAdded.postValue(true);
                _commentAdded.postValue(false); // Reset signal
            }

            @Override
            public void onError(Exception e) {
                _isLoading.postValue(false);
                _error.postValue(e.getMessage());
            }
        });
    }

    // toggleLikeStatus has been removed
}
