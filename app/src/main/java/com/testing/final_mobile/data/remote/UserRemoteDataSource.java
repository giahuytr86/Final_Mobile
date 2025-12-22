package com.testing.final_mobile.data.remote;

import android.util.Log;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.ArrayList;
import java.util.List;

public class UserRemoteDataSource {

    private static final String TAG = "UserRemoteDataSource";
    private static final String USER_COLLECTION = "users";

    private final FirestoreService firestoreService;

    public interface OnUsersSearchedListener {
        void onUsersSearched(List<User> users);
        void onError(Exception e);
    }

    public UserRemoteDataSource(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public void searchUsers(String searchTerm, OnUsersSearchedListener listener) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            listener.onUsersSearched(new ArrayList<>());
            return;
        }

        String lowercasedTerm = searchTerm.toLowerCase();

        Query query = firestoreService.getCollection(USER_COLLECTION)
                .orderBy("searchableName")
                .startAt(lowercasedTerm)
                .endAt(lowercasedTerm + '\uf8ff')
                .limit(20); // Limit results to avoid large data transfer

        firestoreService.getCollection(query, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<User> users = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    User user = doc.toObject(User.class);
                    user.setUid(doc.getId());
                    users.add(user);
                }
                listener.onUsersSearched(users);
            } else {
                Log.e(TAG, "Error searching users", task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
    }
}
