package com.testing.final_mobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.testing.final_mobile.data.model.Comment;

import java.util.List;

@Dao
public interface CommentDao {

    /**
     * Gets all comments (and replies) for a specific post, ordered by creation time.
     * The UI will be responsible for nesting replies under their parent comments.
     */
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC") // Corrected column name
    LiveData<List<Comment>> getCommentsForPost(String postId);

    /**
     * Inserts a list of comments. If a comment already exists, it is replaced.
     * This is used to cache data from the server.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Comment> comments);

    /**
     * Deletes all comments for a specific post.
     */
    @Query("DELETE FROM comments WHERE postId = :postId")
    void deleteCommentsForPost(String postId);

    /**
     * Deletes all comments from the table. Used for cache invalidation if needed.
     */
    @Query("DELETE FROM comments")
    void deleteAll();
}
