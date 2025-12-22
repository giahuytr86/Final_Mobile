package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.repository.UserRepository;

import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<List<User>> _searchResults = new MutableLiveData<>();
    public LiveData<List<User>> searchResults = _searchResults;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
    }

    public void searchUsers(String searchTerm) {
        _isLoading.setValue(true);
        userRepository.searchUsers(searchTerm, _searchResults, _error);
        // A small hack to set loading to false after the search is likely done.
        // A more robust solution would involve callbacks for completion.
        _isLoading.setValue(false);
    }
}
