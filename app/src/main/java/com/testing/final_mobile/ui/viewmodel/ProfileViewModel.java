package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
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

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> _posts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isFollowing = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    private String profileUserId;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository();
        this.postRepository = new PostRepository(application);
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    public void init(String userId) {
        this.profileUserId = userId;
        loadUserProfile();
        loadUserPosts();
        checkIfFollowing();
    }

    private void loadUserProfile() {
        _isLoading.setValue(true);
        userRepository.getUser(profileUserId).observeForever(userData -> {
            _user.setValue(userData);
            _isLoading.setValue(false);
        });
    }

    private void loadUserPosts() {
        postRepository.getAllPosts().observeForever(allPosts -> {
            _posts.setValue(allPosts);
        });
    }

    private void checkIfFollowing() {
        if (currentUserId == null || profileUserId.equals(currentUserId)) return;
        userRepository.getUser(currentUserId).observeForever(currentUser -> {
            if (currentUser != null && currentUser.getFollowing().contains(profileUserId)) {
                _isFollowing.setValue(true);
            } else {
                _isFollowing.setValue(false);
            }
        });
    }

    public void toggleFollow() {
        if (currentUserId == null || profileUserId.equals(currentUserId)) return;
        _isLoading.setValue(true);
        if (Boolean.TRUE.equals(_isFollowing.getValue())) {
            userRepository.unfollowUser(profileUserId, new UserRepository.OnDataCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    _isFollowing.setValue(false);
                    _isLoading.setValue(false);
                }
                @Override
                public void onFailure(Exception e) {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                }
            });
        } else {
            userRepository.followUser(profileUserId, new UserRepository.OnDataCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    _isFollowing.setValue(true);
                    _isLoading.setValue(false);
                }
                @Override
                public void onFailure(Exception e) {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                }
            });
        }
    }

    public void toggleLike(String postId) {
        postRepository.toggleLikeStatus(postId, new PostRepository.OnPostLikedListener() {
            @Override
            public void onPostLiked() {}
            @Override
            public void onError(Exception e) {
                _error.setValue(e.getMessage());
            }
        });
    }

    public LiveData<User> getUser() { return _user; }
    public LiveData<List<Post>> getPosts() { return _posts; }
    public LiveData<Boolean> isFollowing() { return _isFollowing; }
    public LiveData<String> getError() { return _error; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
}
