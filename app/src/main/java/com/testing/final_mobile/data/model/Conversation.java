package com.testing.final_mobile.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import com.testing.final_mobile.data.local.converters.DateConverter;

import java.util.Date;
import java.util.List;

@Entity(tableName = "conversations")
@TypeConverters(DateConverter.class)
public class Conversation {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "conversationId")
    private String conversationId;

    @ColumnInfo(name = "lastMessage")
    private String lastMessage;
    
    @ServerTimestamp
    @ColumnInfo(name = "lastMessageTimestamp") // Đảm bảo Room tạo cột đúng tên này
    private Date lastMessageTimestamp;

    private String otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private List<String> participants;

    public Conversation() {
        this.conversationId = "";
    }

    @PropertyName("message")
    public String getLastMessage() {
        return lastMessage;
    }

    @PropertyName("message")
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @PropertyName("timestamp")
    public Date getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    @PropertyName("timestamp")
    public void setLastMessageTimestamp(Date lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    @NonNull
    public String getConversationId() { return conversationId; }
    public void setConversationId(@NonNull String conversationId) { this.conversationId = conversationId; }
    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    public String getOtherUserAvatar() { return otherUserAvatar; }
    public void setOtherUserAvatar(String otherUserAvatar) { this.otherUserAvatar = otherUserAvatar; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
}
