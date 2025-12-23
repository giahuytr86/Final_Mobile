package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.data.remote.MessageRemoteDataSource;
import com.testing.final_mobile.data.repository.MessageRepository;

import java.util.List;

public class MessageViewModel extends AndroidViewModel {

    private final MessageRepository messageRepository;
    private LiveData<List<ChatMessage>> messages;
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> messageSent = new MutableLiveData<>();

    public MessageViewModel(@NonNull Application application) {
        super(application);
        this.messageRepository = new MessageRepository();
    }

    public void init(String otherUserId) {
        if (messages == null) {
            messages = messageRepository.getMessages(otherUserId);
        }
    }

    public void sendMessage(String text, String receiverId) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        messageRepository.sendMessage(text.trim(), receiverId, new MessageRemoteDataSource.SendMessageListener() {
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
}
