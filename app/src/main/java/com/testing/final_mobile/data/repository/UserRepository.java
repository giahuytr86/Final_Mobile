package com.testing.final_mobile.data.repository;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.remote.UserRemoteDataSource;

import java.util.List;

public class UserRepository {

    private final UserRemoteDataSource remoteDataSource;
    private final FirebaseAuth auth;

    //<editor-fold desc="Interfaces and Callbacks">
    public interface OnDataCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
    //</editor-fold>

    public UserRepository() {
        this.remoteDataSource = new UserRemoteDataSource();
        this.auth = FirebaseAuth.getInstance();
    }

    public void searchUsers(String searchTerm, MutableLiveData<List<User>> searchResults, MutableLiveData<String> error) {
        remoteDataSource.searchUsers(searchTerm, new UserRemoteDataSource.UserCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                searchResults.postValue(result);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    private String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        } else {
            return null;
        }
    }

    public LiveData<User> getUser(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        remoteDataSource.getUser(userId, new UserRemoteDataSource.UserCallback<User>() {
            @Override
            public void onSuccess(User result) {
                userLiveData.postValue(result);
            }

            @Override
            public void onError(Exception e) {
                // In a real app, you might want to post an error state to the LiveData
            }
        });
        return userLiveData;
    }

    public void updateUserProfile(String username, String bio, OnDataCallback<Void> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new Exception("User not authenticated."));
            return;
        }
        remoteDataSource.updateUserProfile(userId, username, bio, new UserRemoteDataSource.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void uploadAvatar(Uri imageUri, OnDataCallback<Uri> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new Exception("User not authenticated."));
            return;
        }
        remoteDataSource.uploadAvatar(userId, imageUri, new UserRemoteDataSource.UserCallback<Uri>() {
            @Override
            public void onSuccess(Uri result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void changePassword(String newPassword, OnDataCallback<Void> callback) {
        remoteDataSource.changePassword(newPassword, new UserRemoteDataSource.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void followUser(String targetUserId, OnDataCallback<Void> callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onFailure(new Exception("User not authenticated."));
            return;
        }
        remoteDataSource.followUser(currentUserId, targetUserId, new UserRemoteDataSource.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void unfollowUser(String targetUserId, OnDataCallback<Void> callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onFailure(new Exception("User not authenticated."));
            return;
        }
        remoteDataSource.unfollowUser(currentUserId, targetUserId, new UserRemoteDataSource.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}
