package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.repository.PostRepository;
import com.testing.final_mobile.data.repository.UserRepository;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final String currentUserId;

    private final MediatorLiveData<User> _user = new MediatorLiveData<>();
    private final MediatorLiveData<List<Post>> _posts = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> _isFollowing = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    private String profileUserId;

    // Keep a reference to the sources to remove them later
    private LiveData<User> userSource;
    private LiveData<List<Post>> postsSource;
    private LiveData<User> followingCheckSource;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        this.postRepository = new PostRepository(application);
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    public void init(String userId) {
        if (userId == null || userId.equals(profileUserId)) {
            return; // Don't re-initialize for the same user
        }
        this.profileUserId = userId;

        _isLoading.setValue(true);

        // --- Clean up old sources ---
        if (userSource != null) _user.removeSource(userSource);
        if (postsSource != null) _posts.removeSource(postsSource);
        if (followingCheckSource != null) _isFollowing.removeSource(followingCheckSource);

        // --- Set up new sources ---
        userSource = userRepository.getUser(profileUserId);
        postsSource = postRepository.getPostsForUser(profileUserId);

        // 1. Observe User Data
        _user.addSource(userSource, user -> {
            _user.setValue(user);
            _isLoading.setValue(false); // Stop loading once user data arrives
            if (user == null) {
                _error.setValue("Failed to load user profile.");
            }
        });

        // 2. Observe Posts Data
        _posts.addSource(postsSource, posts -> _posts.setValue(posts));

        // 3. Observe Following Status
        if (currentUserId != null && !currentUserId.equals(profileUserId)) {
            followingCheckSource = userRepository.getUser(currentUserId);
            _isFollowing.addSource(followingCheckSource, currentUser -> {
                if (currentUser != null && currentUser.getFollowing() != null) {
                    _isFollowing.setValue(currentUser.getFollowing().contains(profileUserId));
                } else {
                    _isFollowing.setValue(false);
                }
            });
        }
    }

    public void toggleFollow() {
        if (currentUserId == null || profileUserId.equals(currentUserId) || _isFollowing.getValue() == null) {
            return;
        }

        UserRepository.OnDataCallback<Void> callback = new UserRepository.OnDataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Data is refreshed automatically by the repository
            }

            @Override
            public void onFailure(Exception e) {
                _error.setValue("Follow/Unfollow failed: " + e.getMessage());
            }
        };

        if (Boolean.TRUE.equals(_isFollowing.getValue())) {
            userRepository.unfollowUser(profileUserId, callback);
        } else {
            userRepository.followUser(profileUserId, callback);
        }
    }

    public void updateAvatar(Uri imageUri) {
        if (currentUserId == null) {
            _error.setValue("User not authenticated.");
            return;
        }
        _isLoading.setValue(true);

        userRepository.uploadAvatar(imageUri, new UserRepository.OnDataCallback<Uri>() {
            @Override
            public void onSuccess(Uri downloadUri) {
                userRepository.updateUserAvatar(currentUserId, downloadUri.toString(), new UserRepository.OnDataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        _isLoading.setValue(false);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        _isLoading.setValue(false);
                        _error.setValue("Failed to update avatar URL: " + e.getMessage());
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                _isLoading.setValue(false);
                _error.setValue("Avatar upload failed: " + e.getMessage());
            }
        });
    }

    public void toggleLike(String postId) {
        postRepository.toggleLikeStatus(postId, new PostRepository.OnPostLikedListener() {
            @Override
            public void onPostLiked() {
                /* UI will update automatically via LiveData from Room */
            }

            @Override
            public void onError(Exception e) {
                _error.setValue("Like failed: " + e.getMessage());
            }
        });
    }

    public LiveData<User> getUser() { return _user; }
    public LiveData<List<Post>> getPosts() { return _posts; }
    public LiveData<Boolean> isFollowing() { return _isFollowing; }
    public LiveData<String> getError() { return _error; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
}
