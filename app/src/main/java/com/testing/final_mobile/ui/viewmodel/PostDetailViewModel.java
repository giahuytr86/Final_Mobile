package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.repository.PostRepository;

public class PostDetailViewModel extends AndroidViewModel {

    private final PostRepository postRepository;

    private LiveData<Post> post;
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        this.postRepository = new PostRepository(application);
    }

    public void fetchPost(String postId) {
        post = postRepository.getPostById(postId);
    }

    public LiveData<Post> getPost() {
        return post;
    }

    public void toggleLikeStatus(String postId) {
        postRepository.toggleLikeStatus(postId, new PostRepository.OnPostLikedListener() {
            @Override
            public void onPostLiked() {
                // The LiveData will update automatically, so we might not need to do anything here.
                // Or we can post a success event if needed.
            }

            @Override
            public void onError(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }
}
