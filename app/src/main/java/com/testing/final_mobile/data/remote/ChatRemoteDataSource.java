package com.testing.final_mobile.data.remote;

import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.model.Conversation;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRemoteDataSource {

    private static final String TAG = "ChatRemoteDS";
    private static final String CHAT_COLLECTION = "chats";
    private static final String MESSAGES_SUB_COLLECTION = "messages";

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

        // 1. Thêm tin nhắn vào sub-collection 'messages'
        var messageRef = db.collection(CHAT_COLLECTION)
                .document(message.getConversationId())
                .collection(MESSAGES_SUB_COLLECTION)
                .document();
        message.setMessageId(messageRef.getId());
        batch.set(messageRef, message);

        // 2. Cập nhật document cha (cuộc hội thoại)
        // Lấy IDs từ conversationId (định dạng user1_user2)
        String[] ids = message.getConversationId().split("_");
        List<String> participants = Arrays.asList(ids);

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", message.getMessage());
        updates.put("lastMessageTimestamp", message.getTimestamp());
        updates.put("participants", participants);
        
        // Lưu ý: Thông tin otherUserName và otherUserAvatar nên được cập nhật từ phía UI 
        // hoặc ChatActivity trước khi gọi sendMessage nếu muốn hiển thị chính xác tên người kia.

        var conversationRef = db.collection(CHAT_COLLECTION).document(message.getConversationId());
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
        // Tìm các cuộc trò chuyện mà mảng 'participants' chứa userId hiện tại
        Query query = firestoreService.getCollection(CHAT_COLLECTION)
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        return query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "Error listening for conversations", e);
                listener.onError(e);
                return;
            }
            if (snapshots != null) {
                List<Conversation> conversations = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Conversation conv = doc.toObject(Conversation.class);
                    if (conv != null) {
                        conv.setConversationId(doc.getId());
                        // Vì model Conversation cần thông tin người kia để hiển thị, 
                        // logic ở đây có thể cần xử lý thêm nếu dữ liệu Firestore chưa có otherUserName/Avatar.
                        conversations.add(conv);
                    }
                }
                listener.onConversationsUpdated(conversations);
            }
        });
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
