package com.testing.final_mobile.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PostDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "posts.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_POSTS = "posts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_USER_AVATAR_URL = "user_avatar_url";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_LIKE_COUNT = "like_count";
    public static final String COLUMN_COMMENT_COUNT = "comment_count";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_POSTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_POST_ID + " TEXT UNIQUE, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_USER_NAME + " TEXT, " +
                    COLUMN_USER_AVATAR_URL + " TEXT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_IMAGE_URL + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER, " +
                    COLUMN_LIKE_COUNT + " INTEGER, " +
                    COLUMN_COMMENT_COUNT + " INTEGER" +
                    ");";

    public PostDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }
}
