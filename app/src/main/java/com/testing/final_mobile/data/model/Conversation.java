package com.example.final_mobile.data.model;

import com.google.firebase.Timestamp;
import java.util.List;

public class Conversation {
    private String conversationId;
    private List<String> participantIds; // List of UIDs involved
    private String lastMessage;
    private Timestamp lastMessageTimestamp;

    public Conversation() { }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Timestamp getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }
}