package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.testing.final_mobile.data.model.Conversation;
import com.testing.final_mobile.data.repository.ChatRepository;

import java.util.List;

public class ConversationViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private final LiveData<List<Conversation>> conversations;

    public ConversationViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
        this.conversations = chatRepository.getConversations();
    }

    public LiveData<List<Conversation>> getConversations() {
        return conversations;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        chatRepository.stopListeningForConversations();
    }
}
