package com.testing.final_mobile.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.firestore.ServerTimestamp;
import com.testing.final_mobile.data.local.converters.DateConverter;

import java.util.Date;

@Entity(tableName = "chat_messages")
@TypeConverters(DateConverter.class)
public class ChatMessage {

    @PrimaryKey
    @NonNull
    private String messageId;

    private String conversationId;
    private String senderId;
    private String senderName;
    private String message;

    @ServerTimestamp
    private Date timestamp;

    public ChatMessage() {
        this.messageId = ""; // Ensure non-null
    }

    //<editor-fold desc="Getters and Setters">
    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    //</editor-fold>
}
