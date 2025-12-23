package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.repository.MessageRepository;

import java.util.List;

public class MessageViewModel extends AndroidViewModel {

    private final MessageRepository messageRepository;
    private final LiveData<List<ChatMessage>> messages;
    private final MutableLiveData<Boolean> _isMessageSent = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    public MessageViewModel(@NonNull Application application, String receiverId) {
        super(application);
        messageRepository = new MessageRepository();
        messages = messageRepository.getMessages(receiverId);
    }

    public void sendMessage(String messageText, String receiverId) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return; // Do not send empty messages
        }
        // In MessageRepository, the listener is of type MessageRemoteDataSource.SendMessageListener
        // Let's assume the repository abstracts this away.
        messageRepository.sendMessage(messageText, receiverId, new MessageRepository.OnMessageSendListener() {
            @Override
            public void onSuccess() {
                _isMessageSent.postValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> isMessageSent() {
        return _isMessageSent;
    }

    public LiveData<String> getError() {
        return _error;
    }
}
