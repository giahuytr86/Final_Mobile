package com.testing.final_mobile.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.testing.final_mobile.data.model.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostDataSource {

    private SQLiteDatabase database;
    private final PostDbHelper dbHelper;

    private final String[] allColumns = {
            PostDbHelper.COLUMN_ID,
            PostDbHelper.COLUMN_POST_ID,
            PostDbHelper.COLUMN_USER_ID,
            PostDbHelper.COLUMN_USER_NAME,
            PostDbHelper.COLUMN_USER_AVATAR_URL,
            PostDbHelper.COLUMN_CONTENT,
            PostDbHelper.COLUMN_IMAGE_URL,
            PostDbHelper.COLUMN_TIMESTAMP,
            PostDbHelper.COLUMN_LIKE_COUNT,
            PostDbHelper.COLUMN_COMMENT_COUNT
    };

    public PostDataSource(Context context) {
        dbHelper = new PostDbHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Post createPost(Post post) {
        ContentValues values = new ContentValues();
        values.put(PostDbHelper.COLUMN_POST_ID, post.getPostId());
        values.put(PostDbHelper.COLUMN_USER_ID, post.getUserId());
        values.put(PostDbHelper.COLUMN_USER_NAME, post.getUserName());
        values.put(PostDbHelper.COLUMN_USER_AVATAR_URL, post.getUserAvatarUrl());
        values.put(PostDbHelper.COLUMN_CONTENT, post.getContent());
        values.put(PostDbHelper.COLUMN_IMAGE_URL, post.getImageUrl());
        if (post.getTimestamp() != null) {
            values.put(PostDbHelper.COLUMN_TIMESTAMP, post.getTimestamp().getTime());
        }
        values.put(PostDbHelper.COLUMN_LIKE_COUNT, post.getLikeCount());
        values.put(PostDbHelper.COLUMN_COMMENT_COUNT, post.getCommentCount());

        long insertId = database.insert(PostDbHelper.TABLE_POSTS, null, values);
        Cursor cursor = database.query(PostDbHelper.TABLE_POSTS, allColumns, PostDbHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        Post newPost = cursorToPost(cursor);
        cursor.close();
        return newPost;
    }
    
    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        Cursor cursor = database.query(PostDbHelper.TABLE_POSTS, allColumns, null, null, null, null, PostDbHelper.COLUMN_TIMESTAMP + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Post post = cursorToPost(cursor);
            posts.add(post);
            cursor.moveToNext();
        }
        cursor.close();
        return posts;
    }

    private Post cursorToPost(Cursor cursor) {
        Post post = new Post();
        post.setPostId(cursor.getString(cursor.getColumnIndexOrThrow(PostDbHelper.COLUMN_POST_ID)));
        post.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(PostDbHelper.COLUMN_USER_ID)));
        post.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(PostDbHelper.COLUMN_USER_NAME)));
        post.setUserAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(PostDbHelper.COLUMN_USER_AVATAR_URL)));
        post.setContent(cursor.getString(cursor.getColumnIndexOrThrow(PostDbHelper.COLUMN_CONTENT)));
        post.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(PostDbHelper.COLUMN_IMAGE_URL)));
        post.setTimestamp(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(PostDbHelper.COLUMN_TIMESTAMP))));
        post.setLikeCount(cursor.getInt(cursor.getColumnIndexOrThrow(PostDbHelper.COLUMN_LIKE_COUNT)));
        post.setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(PostDbHelper.COLUMN_COMMENT_COUNT)));
        return post;
    }
}
