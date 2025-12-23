package com.testing.final_mobile.data.remote;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.testing.final_mobile.data.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRemoteDataSource {

    private static final String TAG = "UserRemoteDataSource";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public UserRemoteDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void searchUsers(String searchTerm, UserCallback<List<User>> callback) {
        // ... (existing code)
    }

    public void getUser(String userId, UserCallback<User> callback) {
        // ... (existing code)
    }

    public void updateUserProfile(String userId, String username, String bio, UserCallback<Void> callback) {
        // ... (existing code)
    }

    public void uploadAvatar(String userId, Uri imageUri, UserCallback<Uri> callback) {
        // ... (existing code)
    }

    // --- This is the missing method ---
    public void updateUserAvatar(String userId, String avatarUrl, UserCallback<Void> callback) {
        if (userId == null || avatarUrl == null) {
            callback.onError(new IllegalArgumentException("User ID and Avatar URL cannot be null"));
            return;
        }
        firestore.collection(USERS_COLLECTION).document(userId)
                .update("avatarUrl", avatarUrl)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void changePassword(String newPassword, UserCallback<Void> callback) {
        // ... (existing code)
    }

    public void followUser(String currentUserId, String targetUserId, UserCallback<Void> callback) {
        // ... (existing code)
    }

    public void unfollowUser(String currentUserId, String targetUserId, UserCallback<Void> callback) {
        // ... (existing code)
    }
}
