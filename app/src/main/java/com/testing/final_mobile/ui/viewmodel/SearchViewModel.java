package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.repository.PostRepository;
import com.testing.final_mobile.data.repository.UserRepository;

import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // --- User Search --- //
    private final MutableLiveData<List<User>> _userSearchResults = new MutableLiveData<>();
    public LiveData<List<User>> userSearchResults = _userSearchResults;

    // --- Post Search --- //
    private final MutableLiveData<List<Post>> _postSearchResults = new MutableLiveData<>();
    public LiveData<List<Post>> postSearchResults = _postSearchResults;

    // --- Common --- //
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        this.postRepository = new PostRepository(application);
    }

    public void searchUsers(String searchTerm) {
        _isLoading.setValue(true);
        userRepository.searchUsers(searchTerm, _userSearchResults, _error);
        _isLoading.postValue(false); // Use postValue for thread safety
    }

    public void searchPosts(String searchTerm) {
        _isLoading.setValue(true);
        postRepository.searchPosts(searchTerm, _postSearchResults, _error);
        _isLoading.postValue(false);
    }
}
