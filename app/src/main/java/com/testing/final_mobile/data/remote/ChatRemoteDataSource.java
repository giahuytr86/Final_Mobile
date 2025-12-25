package com.testing.final_mobile.data.remote;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.model.Conversation;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatRemoteDataSource {

    private static final String TAG = "ChatRemoteDS";
    private static final String CHAT_COLLECTION = "chats";
    private static final String MESSAGES_SUB_COLLECTION = "messages";
    private static final String USER_COLLECTION = "users";

    private final FirestoreService firestoreService;

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

    public ChatRemoteDataSource(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public void sendMessage(ChatMessage message, OnMessageSentListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        String convId = message.getConversationId();
        var messageRef = db.collection(CHAT_COLLECTION)
                .document(convId)
                .collection(MESSAGES_SUB_COLLECTION)
                .document();
        message.setMessageId(messageRef.getId());
        batch.set(messageRef, message);

        String currentUserId = FirebaseAuth.getInstance().getUid();
        String[] ids = convId.split("_");
        String otherUserId = (ids.length == 2) ? (ids[0].equals(currentUserId) ? ids[1] : ids[0]) : "";

        Map<String, Object> updates = new HashMap<>();
        updates.put("message", message.getMessage());
        updates.put("timestamp", message.getTimestamp());
        updates.put("participants", Arrays.asList(ids));
        updates.put("otherUserId", otherUserId);

        var conversationRef = db.collection(CHAT_COLLECTION).document(convId);
        batch.set(conversationRef, updates, SetOptions.merge());

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onMessageSent();
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public ListenerRegistration listenForConversations(String userId, OnConversationsListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection(CHAT_COLLECTION)
                .whereArrayContains("participants", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        return query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                listener.onError(e);
                return;
            }
            if (snapshots != null) {
                List<Conversation> rawConversations = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Conversation conv = doc.toObject(Conversation.class);
                    if (conv != null) {
                        conv.setConversationId(doc.getId());
                        if (conv.getOtherUserId() == null) {
                            String[] ids = doc.getId().split("_");
                            if (ids.length == 2) {
                                conv.setOtherUserId(ids[0].equals(userId) ? ids[1] : ids[0]);
                            }
                        }
                        rawConversations.add(conv);
                    }
                }

                if (rawConversations.isEmpty()) {
                    listener.onConversationsUpdated(new ArrayList<>());
                    return;
                }

                // Lấy thêm thông tin User cho từng cuộc hội thoại
                fetchUserDetailsForConversations(db, rawConversations, listener);
            }
        });
    }

    private void fetchUserDetailsForConversations(FirebaseFirestore db, List<Conversation> conversations, OnConversationsListener listener) {
        AtomicInteger counter = new AtomicInteger(0);
        int total = conversations.size();

        for (Conversation conv : conversations) {
            String otherId = conv.getOtherUserId();
            if (otherId == null || otherId.isEmpty()) {
                if (counter.incrementAndGet() == total) listener.onConversationsUpdated(conversations);
                continue;
            }

            db.collection(USER_COLLECTION).document(otherId).get().addOnSuccessListener(userDoc -> {
                User user = userDoc.toObject(User.class);
                if (user != null) {
                    conv.setOtherUserName(user.getUsername());
                    conv.setOtherUserAvatar(user.getAvatarUrl());
                }
                if (counter.incrementAndGet() == total) {
                    listener.onConversationsUpdated(conversations);
                }
            }).addOnFailureListener(e -> {
                if (counter.incrementAndGet() == total) {
                    listener.onConversationsUpdated(conversations);
                }
            });
        }
    }

    public ListenerRegistration listenForNewMessages(String conversationId, OnNewMessagesListener listener) {
        String messagesPath = CHAT_COLLECTION + "/" + conversationId + "/" + MESSAGES_SUB_COLLECTION;
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
                        if (msg != null) {
                            msg.setMessageId(dc.getDocument().getId());
                            msg.setConversationId(conversationId);
                            newMessages.add(msg);
                        }
                    }
                }
                if (!newMessages.isEmpty()) {
                    listener.onNewMessages(newMessages);
                }
            }
        });
    }
}
