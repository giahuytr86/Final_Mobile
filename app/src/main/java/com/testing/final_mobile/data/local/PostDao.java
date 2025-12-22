package com.testing.final_mobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.testing.final_mobile.data.model.Post;

import java.util.List;

@Dao
public interface PostDao {

    // Returns LiveData, which will automatically update the UI upon data changes.
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    LiveData<List<Post>> getAllPosts();

    // Returns a single post as LiveData.
    @Query("SELECT * FROM posts WHERE id = :postId")
    LiveData<Post> getPostById(String postId);

    // Inserts a list of posts. If a post already exists, it will be replaced.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Post> posts);

    // Deletes all posts from the table.
    @Query("DELETE FROM posts")
    void deleteAll();
}
