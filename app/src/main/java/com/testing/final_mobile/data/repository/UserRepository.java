package com.testing.final_mobile.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.local.AppDatabase;
import com.testing.final_mobile.data.local.UserDao;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.remote.UserRemoteDataSource;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.List;

public class UserRepository {

    private final UserDao userDao;
    private final UserRemoteDataSource remoteDataSource;

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.userDao = database.userDao();
        this.remoteDataSource = new UserRemoteDataSource(new FirestoreService());
    }

    public void searchUsers(String searchTerm, MutableLiveData<List<User>> searchResults, MutableLiveData<String> error) {
        remoteDataSource.searchUsers(searchTerm, new UserRemoteDataSource.OnUsersSearchedListener() {
            @Override
            public void onUsersSearched(List<User> users) {
                // Optionally, cache these users in Room for later use
                // AppDatabase.databaseWriteExecutor.execute(() -> userDao.insertAll(users));
                searchResults.postValue(users);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public LiveData<User> getUserById(String uid) {
        // This method can be used to get user details from the local cache
        return userDao.getUserById(uid);
    }
}
