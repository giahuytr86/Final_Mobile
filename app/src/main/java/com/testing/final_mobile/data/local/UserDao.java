package com.testing.final_mobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.testing.final_mobile.data.model.User;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    // Get a user by their ID and observe changes.
    @Query("SELECT * FROM users WHERE uid = :userId")
    LiveData<User> getUserById(String userId);

    // Used for updating user data in the background.
    @Query("SELECT * FROM users WHERE uid = :userId")
    User getUser(String userId);

}
