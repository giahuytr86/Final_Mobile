package com.testing.final_mobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.testing.final_mobile.data.model.Conversation;

import java.util.List;

@Dao
public interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC")
    LiveData<List<Conversation>> getAllConversations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Conversation> conversations);
}
