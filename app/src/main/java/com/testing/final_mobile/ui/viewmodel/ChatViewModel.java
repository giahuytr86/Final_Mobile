package com.testing.final_mobile.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.final_mobile.data.model.Conversation;
import com.example.final_mobile.data.model.Message;
import com.example.final_mobile.data.repository.ChatRepository;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private final ChatRepository repository;
    private final MutableLiveData<List<Conversation>> conversationList = new MutableLiveData<>();
    private final MutableLiveData<List<Message>> messageList = new MutableLiveData<>();

    public ChatViewModel() {
        repository = new ChatRepository();
    }

    public LiveData<List<Conversation>> getConversations() {
        repository.getConversations(conversationList);
        return conversationList;
    }

    public LiveData<List<Message>> getMessages(String conversationId) {
        repository.getMessages(conversationId, messageList);
        return messageList;
    }

    public void sendMessage(String conversationId, Message message) {
        repository.sendMessage(conversationId, message);
    }
}