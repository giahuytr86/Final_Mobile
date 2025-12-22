package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.repository.PostRepository;
import com.testing.final_mobile.data.repository.UserRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final MediatorLiveData<User> _user = new MediatorLiveData<>();
    public LiveData<User> user = _user;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository();
        this.postRepository = new PostRepository(application);
    }

    public void loadUser(String userId) {
        _isLoading.setValue(true);
        LiveData<User> userSource = userRepository.getUser(userId);

        _user.addSource(userSource, user -> {
            _user.setValue(user);
            _isLoading.setValue(false);
        });
    }

    public void followUser(String userId) {
        userRepository.followUser(userId, new UserRepository.OnDataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // The user LiveData will be updated automatically by the repository
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }

    public void unfollowUser(String userId) {
        userRepository.unfollowUser(userId, new UserRepository.OnDataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // The user LiveData will be updated automatically by the repository
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }

    public void toggleLikeStatus(String postId) {
        postRepository.toggleLikeStatus(postId, new PostRepository.OnPostLikedListener() {
            @Override
            public void onPostLiked() {
                // The post will be refreshed in the repository, and UI will update via LiveData.
            }

            @Override
            public void onError(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }
}
