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
import com.google.firebase.storage.FirebaseStorage; // Added import
import com.google.firebase.storage.StorageReference; // Added import
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

    //<editor-fold desc="Interfaces">
    public interface UserCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
    //</editor-fold>

    public UserRemoteDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void searchUsers(String searchTerm, UserCallback<List<User>> callback) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        String lowercasedTerm = searchTerm.toLowerCase();

        firestore.collection(USERS_COLLECTION)
                .orderBy("username")
                .startAt(lowercasedTerm)
                .endAt(lowercasedTerm + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        users.add(doc.toObject(User.class));
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getUser(String userId, UserCallback<User> callback) {
        firestore.collection(USERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateUserProfile(String userId, String username, String bio, UserCallback<Void> callback) {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
        userRef.update("username", username, "bio", bio)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void uploadAvatar(String userId, Uri imageUri, UserCallback<Uri> callback) {
        String imagePath = "avatars/" + userId + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference avatarRef = storage.getReference(imagePath);

        avatarRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return avatarRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUrl -> {
                    // After getting the URL, update the user's profile in Firestore
                    firestore.collection(USERS_COLLECTION).document(userId)
                            .update("avatarUrl", downloadUrl.toString())
                            .addOnSuccessListener(aVoid -> callback.onSuccess(downloadUrl))
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void changePassword(String newPassword, UserCallback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(task.getException());
                        }
                    });
        } else {
            callback.onError(new Exception("No authenticated user found."));
        }
    }

    public void followUser(String currentUserId, String targetUserId, UserCallback<Void> callback) {
        WriteBatch batch = firestore.batch();

        // Add target to current user's following list
        DocumentReference currentUserRef = firestore.collection(USERS_COLLECTION).document(currentUserId);
        batch.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId));

        // Add current user to target user's followers list
        DocumentReference targetUserRef = firestore.collection(USERS_COLLECTION).document(targetUserId);
        batch.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId));

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void unfollowUser(String currentUserId, String targetUserId, UserCallback<Void> callback) {
        WriteBatch batch = firestore.batch();

        // Remove target from current user's following list
        DocumentReference currentUserRef = firestore.collection(USERS_COLLECTION).document(currentUserId);
        batch.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId));

        // Remove current user from target user's followers list
        DocumentReference targetUserRef = firestore.collection(USERS_COLLECTION).document(targetUserId);
        batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId));

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError(task.getException());
            }
        });
    }
}
