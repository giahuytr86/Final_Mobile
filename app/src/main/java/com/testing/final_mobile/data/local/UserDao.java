package com.testing.final_mobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.testing.final_mobile.data.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users WHERE uid = :uid")
    LiveData<User> getUserById(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    // You can add more specific queries here in the future if needed
}
