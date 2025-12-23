package com.testing.final_mobile.data.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.testing.final_mobile.data.model.User;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String message);
    }

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void registerUser(String email, String password, String username, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User newUser = new User(firebaseUser.getUid(), username, email, "");

                            firestore.collection("users").document(firebaseUser.getUid())
                                    .set(newUser)
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful()) {
                                            callback.onSuccess(firebaseUser);
                                        } else {
                                            callback.onError(userTask.getException().getMessage());
                                        }
                                    });
                        } else {
                            callback.onError("Failed to get user after creation.");
                        }
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(task.getResult().getUser());
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public void updateFcmToken(String userId, String token) {
        if (userId != null && token != null) {
            firestore.collection("users").document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM Token updated successfully for user: " + userId))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating FCM Token", e));
        }
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}
