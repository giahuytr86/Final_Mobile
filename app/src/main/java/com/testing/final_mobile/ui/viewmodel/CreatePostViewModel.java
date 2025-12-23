package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.testing.final_mobile.data.repository.PostRepository;

public class CreatePostViewModel extends AndroidViewModel {

    private final PostRepository postRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _postCreated = new MutableLiveData<>(false);
    public final LiveData<Boolean> postCreated = _postCreated;

    public CreatePostViewModel(@NonNull Application application) {
        super(application);
        this.postRepository = new PostRepository(application);
    }

    public void createPost(String content, Uri imageUri) {
        _isLoading.setValue(true);
        postRepository.createPost(content, imageUri, new PostRepository.OnPostCreatedListener() {
            @Override
            public void onPostCreated() {
                _isLoading.postValue(false);
                _postCreated.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.postValue(false);
                _error.postValue(e.getMessage());
            }
        });
    }
}
