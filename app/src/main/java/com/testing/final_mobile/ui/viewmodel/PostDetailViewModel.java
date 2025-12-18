package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.testing.final_mobile.data.model.Post;

public class PostDetailViewModel extends AndroidViewModel {

    private final FirebaseFirestore db;
    private final MutableLiveData<Post> _post = new MutableLiveData<>();
    public LiveData<Post> post = _post;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
    }

    public void fetchPost(String postId) {
        if (postId == null || postId.isEmpty()) {
            _error.setValue("Post ID is missing.");
            return;
        }

        db.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post fetchedPost = documentSnapshot.toObject(Post.class);
                        if (fetchedPost != null) {
                            fetchedPost.setPostId(documentSnapshot.getId());
                            _post.setValue(fetchedPost);
                        }
                    } else {
                        _error.setValue("Post not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    _error.setValue("Failed to load post: " + e.getMessage());
                });
    }
}
