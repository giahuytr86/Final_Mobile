package com.testing.final_mobile.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.testing.final_mobile.data.local.converters.DateConverter;
import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.data.model.Conversation;
import com.testing.final_mobile.data.model.Post;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Add ChatMessage.class and Conversation.class to entities, increment version to 3
@Database(entities = {Post.class, Comment.class, ChatMessage.class, Conversation.class}, version = 3, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract PostDao postDao();
    public abstract CommentDao commentDao();
    public abstract ChatMessageDao chatMessageDao(); // Add new DAO
    public abstract ConversationDao conversationDao(); // Add new DAO

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
