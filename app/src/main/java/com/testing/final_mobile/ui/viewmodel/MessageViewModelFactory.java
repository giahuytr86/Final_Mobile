package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MessageViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final String receiverId;

    public MessageViewModelFactory(Application application, String receiverId) {
        this.application = application;
        this.receiverId = receiverId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MessageViewModel.class)) {
            return (T) new MessageViewModel(application, receiverId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
