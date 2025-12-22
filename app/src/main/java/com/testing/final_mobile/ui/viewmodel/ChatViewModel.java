package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.repository.ChatRepository;

import java.util.Date;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private LiveData<List<ChatMessage>> messages;

    private final MutableLiveData<Boolean> _isSending = new MutableLiveData<>(false);
    public LiveData<Boolean> isSending = _isSending;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
    }

    public LiveData<List<ChatMessage>> getMessagesForConversation(String conversationId) {
        if (messages == null) {
            messages = chatRepository.getMessagesForConversation(conversationId);
        }
        return messages;
    }

    public void sendMessage(String conversationId, String text) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            _error.setValue("User not logged in");
            return;
        }

        _isSending.setValue(true);

        ChatMessage newMessage = new ChatMessage();
        newMessage.setConversationId(conversationId);
        newMessage.setMessage(text);
        newMessage.setSenderId(currentUser.getUid());
        newMessage.setSenderName(currentUser.getDisplayName());
        newMessage.setTimestamp(new Date()); // Will be replaced by server timestamp

        chatRepository.sendMessage(newMessage, new ChatRepository.OnMessageSentListener() {
            @Override
            public void onMessageSent() {
                _isSending.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _isSending.setValue(false);
                _error.setValue(e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        chatRepository.stopListeningForMessages();
    }
}
