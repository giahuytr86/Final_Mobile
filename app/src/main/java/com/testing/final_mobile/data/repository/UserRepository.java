package com.testing.final_mobile.data.repository;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.data.local.AppDatabase;
import com.testing.final_mobile.data.local.UserDao;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.remote.UserRemoteDataSource;

import java.util.List;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private final UserRemoteDataSource remoteDataSource;
    private final UserDao userDao;
    private final FirebaseAuth auth;

    public interface OnDataCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.userDao = database.userDao();
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
        }
        return null;
    }

    public LiveData<User> getUser(String userId) {
        refreshUser(userId); // Attempt to refresh data from remote
        return userDao.getUserById(userId); // Return LiveData from local DB
    }

    public void refreshUser(String userId) {
        if (userId == null) return;
        remoteDataSource.getUser(userId, new UserRemoteDataSource.UserCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (result != null) {
                    AppDatabase.databaseWriteExecutor.execute(() -> userDao.insert(result));
                } else {
                    // This case is also an error - user not found in remote
                    Log.w(TAG, "User " + userId + " not found in Firestore.");
                }
            }

            @Override
            public void onError(Exception e) {
                // CRITICAL FIX: Log the error that was being swallowed
                Log.e(TAG, "Error fetching user " + userId + " from remote.", e);
            }
        });
    }

    public void updateUserAvatar(String userId, String avatarUrl, OnDataCallback<Void> callback) {
        remoteDataSource.updateUserAvatar(userId, avatarUrl, new UserRemoteDataSource.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                refreshUser(userId); // Refresh after update
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onFailure(e);
            }
        });
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
                refreshUser(userId); // Refresh after update
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

    public void followUser(String targetUserId, OnDataCallback<Void> callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onFailure(new Exception("User not authenticated."));
            return;
        }
        remoteDataSource.followUser(currentUserId, targetUserId, new UserRemoteDataSource.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                refreshUser(currentUserId); // Refresh both users to update follower/following lists
                refreshUser(targetUserId);
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
                refreshUser(currentUserId);
                refreshUser(targetUserId);
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
            public void onSuccess(Void data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}
