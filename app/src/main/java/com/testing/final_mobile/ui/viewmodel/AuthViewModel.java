package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.testing.final_mobile.data.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<FirebaseUser> _user = new MutableLiveData<>();
    public LiveData<FirebaseUser> user = _user;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository();
    }

    public void register(String email, String password, String fullName) {
        _isLoading.setValue(true);
        authRepository.registerUser(email, password, fullName, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                _user.postValue(firebaseUser);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
                _isLoading.postValue(false);
            }
        });
    }

    public void login(String email, String password) {
        _isLoading.setValue(true);
        authRepository.loginUser(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                _user.postValue(firebaseUser);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
                _isLoading.postValue(false);
            }
        });
    }

    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }
}
