package com.testing.final_mobile.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.firestore.ServerTimestamp;
import com.testing.final_mobile.data.local.converters.DateConverter;

import java.util.Date;

@Entity(tableName = "conversations")
@TypeConverters(DateConverter.class)
public class Conversation {

    @PrimaryKey
    @NonNull
    private String conversationId;

    private String lastMessage;
    @ServerTimestamp
    private Date lastMessageTimestamp;

    private String otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private int unreadCount;

    public Conversation() {
        this.conversationId = ""; // Ensure non-null
    }

    //<editor-fold desc="Getters and Setters">
    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Date lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserAvatar() {
        return otherUserAvatar;
    }

    public void setOtherUserAvatar(String otherUserAvatar) {
        this.otherUserAvatar = otherUserAvatar;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
    //</editor-fold>
}
