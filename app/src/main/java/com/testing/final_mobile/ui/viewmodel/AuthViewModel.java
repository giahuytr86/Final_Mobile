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

    // Holds the user object
    private final MutableLiveData<FirebaseUser> _user = new MutableLiveData<>();
    public LiveData<FirebaseUser> user = _user;

    // Holds any error messages
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    // Indicates loading state
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    // Event for logout completion
    private final MutableLiveData<Boolean> _loggedOutEvent = new MutableLiveData<>();
    public LiveData<Boolean> loggedOutEvent = _loggedOutEvent;

    // Static field to hold a pending FCM token that was received when the user was not logged in.
    private static final MutableLiveData<String> pendingFcmToken = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository();
    }

    public void register(String email, String password, String username) {
        _isLoading.setValue(true);
        authRepository.registerUser(email, password, username, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                _user.postValue(firebaseUser);
                checkForPendingToken(firebaseUser.getUid());
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
                checkForPendingToken(firebaseUser.getUid());
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
                _isLoading.postValue(false);
            }
        });
    }

    public void logout() {
        authRepository.logout();
        _loggedOutEvent.setValue(true);
    }

    /**
     * Called from the messaging service when a new token is available.
     * It can be called at any time, even when no user is logged in.
     */
    public static void setPendingFcmToken(String token) {
        pendingFcmToken.postValue(token);
    }

    /**
     * After a user logs in or registers, this method checks if there is a pending
     * FCM token and updates it in the user's Firestore document.
     */
    private void checkForPendingToken(String userId) {
        if (pendingFcmToken.getValue() != null) {
            String token = pendingFcmToken.getValue();
            authRepository.updateFcmToken(userId, token);
            // Clear the pending token once it's being processed
            pendingFcmToken.setValue(null);
        }
    }
}
