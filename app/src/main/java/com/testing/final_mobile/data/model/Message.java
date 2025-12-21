package com.example.final_mobile.data.model;

import com.google.firebase.Timestamp;

public class Message {
    private String senderId;
    private String message;
    private Timestamp timestamp;

    public Message() { } // Firestore requires empty constructor

    public Message(String senderId, String message, Timestamp timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public Timestamp getTimestamp() { return timestamp; }
}