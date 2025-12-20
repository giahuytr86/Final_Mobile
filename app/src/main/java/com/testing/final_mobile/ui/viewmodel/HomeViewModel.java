package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.repository.PostRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final PostRepository postRepository;
    private final LiveData<List<Post>> allPosts;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        // 1. Initialize the repository
        postRepository = new PostRepository(application);
        // 2. Get the LiveData from the repository
        allPosts = postRepository.getAllPosts();
    }

    /**
     * 3. Expose the LiveData for the UI to observe. This LiveData is now sourced
     * from the Room database and is automatically updated when the repository
     * fetches new data from Firebase.
     */
    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }
}
