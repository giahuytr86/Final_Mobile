package com.testing.final_mobile.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.remote.MessageRemoteDataSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageRepository {

    private final MessageRemoteDataSource remoteDataSource;
    private final String currentUserId;

    public MessageRepository() {
        this.remoteDataSource = new MessageRemoteDataSource();
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    private String getConversationId(String user1Id, String user2Id) {
        List<String> ids = Arrays.asList(user1Id, user2Id);
        Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }

    public LiveData<List<ChatMessage>> getMessages(String otherUserId) {
        MutableLiveData<List<ChatMessage>> messagesData = new MutableLiveData<>();
        if (currentUserId == null) return messagesData;

        remoteDataSource.getMessages(currentUserId, otherUserId, new MessageRemoteDataSource.MessagesListener() {
            @Override
            public void onMessagesReceived(List<ChatMessage> messages) {
                messagesData.postValue(messages);
            }

            @Override
            public void onError(Exception e) {
                // Handle error appropriately
            }
        });
        return messagesData;
    }

    public void sendMessage(String text, String receiverId, MessageRemoteDataSource.SendMessageListener listener) {
        if (currentUserId == null) {
            listener.onError(new Exception("User not logged in."));
            return;
        }

        String conversationId = getConversationId(currentUserId, receiverId);
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setMessage(text);
        // senderName could be set here if you have it readily available

        remoteDataSource.sendMessage(message, listener);
    }
}
