package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.repository.PostRepository;

public class PostDetailViewModel extends AndroidViewModel {

    private final PostRepository postRepository;

    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        postRepository = new PostRepository(application);
    }

    /**
     * Gets a single post by its ID. The LiveData is sourced from the Room database
     * and is automatically updated when the repository fetches new data.
     */
    public LiveData<Post> getPostById(String postId) {
        return postRepository.getPostById(postId);
    }

}
