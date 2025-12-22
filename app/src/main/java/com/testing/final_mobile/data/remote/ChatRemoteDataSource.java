package com.testing.final_mobile.data.remote;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.model.Conversation;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.ArrayList;
import java.util.List;

public class ChatRemoteDataSource {

    private static final String TAG = "ChatRemoteDS";
    private static final String CONVERSATION_COLLECTION = "conversations";
    private static final String MESSAGES_SUB_COLLECTION = "messages";

    private final FirestoreService firestoreService;

    //<editor-fold desc="Interfaces">
    public interface OnNewMessagesListener {
        void onNewMessages(List<ChatMessage> newMessages);
        void onError(Exception e);
    }

    public interface OnConversationsListener {
        void onConversationsUpdated(List<Conversation> conversations);
        void onError(Exception e);
    }

    public interface OnMessageSentListener {
        void onMessageSent();
        void onError(Exception e);
    }
    //</editor-fold>

    public ChatRemoteDataSource(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public void sendMessage(ChatMessage message, OnMessageSentListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // 1. Add new message to the messages sub-collection
        DocumentReference messageRef = db.collection(CONVERSATION_COLLECTION)
                .document(message.getConversationId())
                .collection(MESSAGES_SUB_COLLECTION)
                .document(); // Auto-generate ID
        message.setMessageId(messageRef.getId());
        batch.set(messageRef, message);

        // 2. Update the parent conversation document
        DocumentReference conversationRef = db.collection(CONVERSATION_COLLECTION).document(message.getConversationId());
        batch.update(conversationRef, "lastMessage", message.getMessage());
        batch.update(conversationRef, "lastMessageTimestamp", message.getTimestamp());

        // 3. Commit the batch
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onMessageSent();
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public ListenerRegistration listenForNewMessages(String conversationId, OnNewMessagesListener listener) {
        String messagesPath = CONVERSATION_COLLECTION + "/" + conversationId + "/" + MESSAGES_SUB_COLLECTION;
        Query query = firestoreService.getCollection(messagesPath).orderBy("timestamp");

        return query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                listener.onError(e);
                return;
            }
            if (snapshots != null) {
                List<ChatMessage> newMessages = new ArrayList<>();
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        ChatMessage msg = dc.getDocument().toObject(ChatMessage.class);
                        msg.setMessageId(dc.getDocument().getId());
                        msg.setConversationId(conversationId);
                        newMessages.add(msg);
                    }
                }
                if (!newMessages.isEmpty()) {
                    listener.onNewMessages(newMessages);
                }
            }
        });
    }

    public ListenerRegistration listenForConversations(String userId, OnConversationsListener listener) {
        // This query assumes conversations store participants in an array field `participants`
        // For simplicity, let's assume we can query conversations where the user is a participant.
        // A more complex data model might be needed for efficient querying.
        Query query = firestoreService.getCollection(CONVERSATION_COLLECTION)
                .whereArrayContains("participants", userId) // Assumes a `participants` array field
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        return query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                listener.onError(e);
                return;
            }
            if (snapshots != null) {
                List<Conversation> conversations = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Conversation conv = doc.toObject(Conversation.class);
                    if (conv != null) {
                        conv.setConversationId(doc.getId());
                        conversations.add(conv);
                    }
                }
                listener.onConversationsUpdated(conversations);
            }
        });
    }
}
