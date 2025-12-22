package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.repository.PostRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";

    private final PostRepository postRepository;
    private final LiveData<List<Post>> allPosts;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        postRepository = new PostRepository(application);
        allPosts = postRepository.getAllPosts();
    }

    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }

    public void toggleLike(String postId) {
        postRepository.toggleLikeStatus(postId, new PostRepository.OnPostLikedListener() {
            @Override
            public void onPostLiked() {
                // The UI will be updated automatically by the LiveData from Room.
                // We can log this for debugging.
                Log.d(TAG, "Like status toggled for post: " + postId);
            }

            @Override
            public void onError(Exception e) {
                _error.setValue(e.getMessage());
            }
        });
    }
}
