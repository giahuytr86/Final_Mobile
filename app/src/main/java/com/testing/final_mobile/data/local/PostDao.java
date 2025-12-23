package com.testing.final_mobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.testing.final_mobile.data.model.Post;

import java.util.List;

@Dao
public interface PostDao {

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    LiveData<List<Post>> getAllPosts();

    @Query("SELECT * FROM posts WHERE id = :postId")
    LiveData<Post> getPostById(String postId);

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<Post>> getPostsForUser(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Post post);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Post> posts);

    @Query("DELETE FROM posts")
    void deleteAll();

    @Transaction
    public default void deleteAllAndInsertAll(List<Post> posts) {
        deleteAll();
        insertAll(posts);
    }
}
