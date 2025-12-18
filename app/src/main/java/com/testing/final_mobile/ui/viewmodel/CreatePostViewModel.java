package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.testing.final_mobile.data.model.Post;

public class CreatePostViewModel extends AndroidViewModel {

    private final MutableLiveData<String> postContent = new MutableLiveData<String>("");
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    private final MutableLiveData<Boolean> _postSuccessful = new MutableLiveData<>(false);
    public LiveData<Boolean> postSuccessful = _postSuccessful;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;


    public CreatePostViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<String> getPostContent() {
        return postContent;
    }

    public void setPostContent(String content) {
        postContent.setValue(content);
    }

    public void createPost() {
        String content = postContent.getValue();
        if (content == null || content.trim().isEmpty()) {
            _error.setValue("Content cannot be empty");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            _error.setValue("You need to be logged in to post.");
            return;
        }

        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();
        String userAvatarUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "";

        Post newPost = new Post(userId, userName, userAvatarUrl, content.trim(), null);

        db.collection("posts")
                .add(newPost)
                .addOnSuccessListener(documentReference -> {
                    _postSuccessful.setValue(true);
                })
                .addOnFailureListener(e -> {
                    _error.setValue("Error: " + e.getMessage());
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
