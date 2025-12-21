package com.example.final_mobile.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.final_mobile.data.model.Conversation;
import com.example.final_mobile.data.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Listen for changes in the list of conversations
    public void getConversations(MutableLiveData<List<Conversation>> liveData) {
        db.collection("conversations")
                .whereArrayContains("participantIds", currentUserId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Conversation> list = new ArrayList<>();
                    for (var doc : value.getDocuments()) {
                        Conversation conv = doc.toObject(Conversation.class);
                        if (conv != null) {
                            conv.setConversationId(doc.getId());
                            list.add(conv);
                        }
                    }
                    liveData.postValue(list);
                });
    }

    // Listen for real-time messages in a specific chat
    public void getMessages(String conversationId, MutableLiveData<List<Message>> liveData) {
        db.collection("conversations").document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Message> list = new ArrayList<>();
                    for (var doc : value.getDocuments()) {
                        list.add(doc.toObject(Message.class));
                    }
                    liveData.postValue(list);
                });
    }

    // Send a message
    public void sendMessage(String conversationId, Message message) {
        db.collection("conversations").document(conversationId)
                .collection("messages")
                .add(message);

        // Update the outer conversation details
        db.collection("conversations").document(conversationId)
                .update(
                        "lastMessage", message.getMessage(),
                        "lastMessageTimestamp", message.getTimestamp()
                );
    }
}