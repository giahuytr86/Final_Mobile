package com.testing.final_mobile.data.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.testing.final_mobile.data.model.ChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageRemoteDataSource {

    private static final String CHAT_COLLECTION = "chats";
    private static final String MESSAGES_SUB_COLLECTION = "messages";

    private final FirebaseFirestore firestore;

    public interface MessagesListener {
        void onMessagesReceived(List<ChatMessage> messages);
        void onError(Exception e);
    }

    public interface SendMessageListener {
        void onMessageSent();
        void onError(Exception e);
    }

    public MessageRemoteDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    private String getConversationId(String user1Id, String user2Id) {
        List<String> ids = Arrays.asList(user1Id, user2Id);
        Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }

    public void getMessages(String user1Id, String user2Id, MessagesListener listener) {
        String conversationId = getConversationId(user1Id, user2Id);
        firestore.collection(CHAT_COLLECTION).document(conversationId).collection(MESSAGES_SUB_COLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshots == null) return;

                    List<ChatMessage> messages = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        message.setMessageId(doc.getId());
                        messages.add(message);
                    }
                    listener.onMessagesReceived(messages);
                });
    }

    public void sendMessage(ChatMessage message, SendMessageListener listener) {
        getChatCollection(message.getConversationId())
                .add(message)
                .addOnSuccessListener(documentReference -> listener.onMessageSent())
                .addOnFailureListener(listener::onError);
    }
    
    private CollectionReference getChatCollection(String conversationId) {
        return firestore.collection(CHAT_COLLECTION).document(conversationId).collection(MESSAGES_SUB_COLLECTION);
    }
}
