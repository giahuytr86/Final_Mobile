package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.repository.PostRepository;

public class CreatePostViewModel extends AndroidViewModel {

    private static final String TAG = "CreatePostViewModel";

    private final PostRepository postRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<Boolean> _isPostCreated = new MutableLiveData<>(false);
    public LiveData<Boolean> isPostCreated = _isPostCreated;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public CreatePostViewModel(@NonNull Application application) {
        super(application);
        postRepository = new PostRepository(application);
    }

    public void createPost(String content, String imageUrl) {
        _isLoading.setValue(true);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            _error.setValue("User not logged in.");
            _isLoading.setValue(false);
            return;
        }

        // Fetch user details to include in the post
        // NOTE: In a larger app, this user data should be sourced from a UserRepository
        String userId = firebaseUser.getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            Post newPost = new Post();
                            newPost.setUserId(userId);
                            newPost.setUserName(user.getDisplayName());
                            newPost.setUserAvatarUrl(user.getAvatar());
                            newPost.setContent(content);
                            newPost.setImageUrl(imageUrl);

                            postRepository.createPost(newPost, new PostRepository.OnPostCreatedListener() {
                                @Override
                                public void onPostCreated() {
                                    _isLoading.setValue(false);
                                    _isPostCreated.setValue(true);
                                }

                                @Override
                                public void onError(Exception e) {
                                    _isLoading.setValue(false);
                                    _error.setValue(e.getMessage());
                                }
                            });
                        } else {
                            _isLoading.setValue(false);
                            _error.setValue("Could not retrieve user data.");
                        }
                    } else {
                        _isLoading.setValue(false);
                        _error.setValue("User data not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    _isLoading.setValue(false);
                    _error.setValue("Failed to fetch user data: " + e.getMessage());
                    Log.e(TAG, "Failed to fetch user data", e);
                });
    }
}
