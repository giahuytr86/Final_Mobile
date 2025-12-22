package com.testing.final_mobile.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.testing.final_mobile.data.local.AppDatabase;
import com.testing.final_mobile.data.local.ChatMessageDao;
import com.testing.final_mobile.data.local.ConversationDao;
import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.model.Conversation;
import com.testing.final_mobile.data.remote.ChatRemoteDataSource;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.List;

public class ChatRepository {

    private final ChatMessageDao chatMessageDao;
    private final ConversationDao conversationDao;
    private final ChatRemoteDataSource remoteDataSource;

    private ListenerRegistration messageListenerRegistration;
    private ListenerRegistration conversationListenerRegistration;

    public interface OnMessageSentListener {
        void onMessageSent();
        void onError(Exception e);
    }

    public ChatRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.chatMessageDao = database.chatMessageDao();
        this.conversationDao = database.conversationDao();
        this.remoteDataSource = new ChatRemoteDataSource(new FirestoreService());
    }

    public void sendMessage(ChatMessage message, OnMessageSentListener listener) {
        remoteDataSource.sendMessage(message, new ChatRemoteDataSource.OnMessageSentListener() {
            @Override
            public void onMessageSent() {
                // The realtime listener will handle caching the new message.
                listener.onMessageSent();
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public LiveData<List<ChatMessage>> getMessagesForConversation(String conversationId) {
        stopListeningForMessages();
        messageListenerRegistration = remoteDataSource.listenForNewMessages(conversationId, new ChatRemoteDataSource.OnNewMessagesListener() {
            @Override
            public void onNewMessages(List<ChatMessage> newMessages) {
                AppDatabase.databaseWriteExecutor.execute(() -> chatMessageDao.insertAll(newMessages));
            }

            @Override
            public void onError(Exception e) { /* Handle error */ }
        });
        return chatMessageDao.getMessagesForConversation(conversationId);
    }

    public LiveData<List<Conversation>> getConversations() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return conversationDao.getAllConversations(); // Return empty/cached if not logged in

        stopListeningForConversations();
        conversationListenerRegistration = remoteDataSource.listenForConversations(currentUserId, new ChatRemoteDataSource.OnConversationsListener() {
            @Override
            public void onConversationsUpdated(List<Conversation> conversations) {
                AppDatabase.databaseWriteExecutor.execute(() -> conversationDao.insertAll(conversations));
            }

            @Override
            public void onError(Exception e) { /* Handle error */ }
        });

        return conversationDao.getAllConversations();
    }

    public void stopListeningForMessages() {
        if (messageListenerRegistration != null) {
            messageListenerRegistration.remove();
        }
    }

    public void stopListeningForConversations() {
        if (conversationListenerRegistration != null) {
            conversationListenerRegistration.remove();
        }
    }
}
