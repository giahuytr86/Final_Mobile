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

    private final MutableLiveData<List<User>> _userSearchResults = new MutableLiveData<>();
    public LiveData<List<User>> userSearchResults = _userSearchResults;

    private final MutableLiveData<List<Post>> _postSearchResults = new MutableLiveData<>();
    public LiveData<List<Post>> postSearchResults = _postSearchResults;

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
        if(searchTerm.isEmpty()){
            _userSearchResults.setValue(null);
            return;
        }
        _isLoading.setValue(true);
        userRepository.searchUsers(searchTerm, _userSearchResults, _error);
    }

    public void searchPosts(String searchTerm) {
        if(searchTerm.isEmpty()){
            _postSearchResults.setValue(null);
            return;
        }
        _isLoading.setValue(true);
        postRepository.searchPosts(searchTerm, new PostRepository.OnPostsSearchedListener() {
            @Override
            public void onPostsSearched(List<Post> posts) {
                _isLoading.setValue(false);
                _postSearchResults.postValue(posts);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _error.postValue(e.getMessage());
            }
        });
    }

    public void toggleLikeStatus(String postId) {
        postRepository.toggleLikeStatus(postId, new PostRepository.OnPostLikedListener() {
            @Override
            public void onPostLiked() {
                // The LiveData in the repository will trigger an update in the UI.
            }

            @Override
            public void onError(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }
}
