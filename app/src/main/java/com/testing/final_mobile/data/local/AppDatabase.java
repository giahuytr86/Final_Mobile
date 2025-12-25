package com.testing.final_mobile.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.data.model.Conversation;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Incremented version to 6 due to schema changes in Conversation entity (added participants, etc.).
@Database(entities = {Post.class, Comment.class, ChatMessage.class, Conversation.class, User.class}, version = 6, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PostDao postDao();
    public abstract CommentDao commentDao();
    public abstract ChatMessageDao chatMessageDao();
    public abstract ConversationDao conversationDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            // Destructive migration is enabled, so incrementing the version will clear the DB.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
