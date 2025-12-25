package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.repository.ChatRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private LiveData<List<ChatMessage>> messages;
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> messageSent = new MutableLiveData<>();

    public MessageViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
    }

    private String getConversationId(String user1Id, String user2Id) {
        List<String> ids = Arrays.asList(user1Id, user2Id);
        Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }

    public void init(String otherUserId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null || otherUserId == null) return;
        
        String conversationId = getConversationId(currentUserId, otherUserId);
        if (messages == null) {
            messages = chatRepository.getMessagesForConversation(conversationId);
        }
    }

    public void sendMessage(String text, String receiverId) {
        if (text == null || text.trim().isEmpty() || receiverId == null) {
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        String conversationId = getConversationId(currentUserId, receiverId);
        
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setMessage(text.trim());
        // Bạn có thể set senderName nếu cần

        chatRepository.sendMessage(message, new ChatRepository.OnMessageSentListener() {
            @Override
            public void onMessageSent() {
                messageSent.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> isMessageSent() {
        return messageSent;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        chatRepository.stopListeningForMessages();
    }
}
