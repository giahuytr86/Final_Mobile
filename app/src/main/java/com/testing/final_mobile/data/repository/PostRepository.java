package com.testing.final_mobile.data.repository;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.testing.final_mobile.data.local.AppDatabase;
import com.testing.final_mobile.data.local.PostDao;
import com.testing.final_mobile.data.model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostRepository {

    private static final String TAG = "PostRepository";

    private final PostDao postDao;
    private final FirebaseFirestore remoteDb;
    private final Application application;

    public PostRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.postDao = database.postDao();
        this.remoteDb = FirebaseFirestore.getInstance();
        this.application = application;
    }

    // --- Public API for ViewModels ---

    /**
     * Gets all posts. The LiveData is directly from the local Room database.
     * The repository will attempt to refresh the local data from the remote source.
     */
    public LiveData<List<Post>> getAllPosts() {
        refreshPostsFromServer(); // Attempt to refresh data from Firebase
        return postDao.getAllPosts(); // Return local data immediately
    }

    /**
     * Gets a single post. The LiveData is directly from the local Room database.
     * The repository will attempt to refresh the local data from the remote source.
     */
    public LiveData<Post> getPostById(String postId) {
        refreshPostFromServer(postId); // Attempt to refresh data for this specific post
        return postDao.getPostById(postId);
    }

    // --- Data Refreshing Logic ---

    /**
     * Fetches all posts from Firebase and stores them in the local database.
     */
    private void refreshPostsFromServer() {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network not available. Skipping server refresh.");
            return;
        }

        remoteDb.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> postList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        post.setPostId(doc.getId());
                        postList.add(post);
                    }
                    // Save the fetched data to the local database
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        postDao.insertAll(postList);
                        Log.d(TAG, "Successfully refreshed " + postList.size() + " posts from server.");
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching posts from server", e));
    }

    /**
     * Fetches a single post from Firebase and updates it in the local database.
     */
    private void refreshPostFromServer(String postId) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network not available. Skipping server refresh for post: " + postId);
            return;
        }

        remoteDb.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(documentSnapshot.getId());
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                postDao.insertAll(java.util.Collections.singletonList(post));
                                Log.d(TAG, "Successfully refreshed post " + postId + " from server.");
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching post " + postId + " from server", e));
    }

    // --- Utility Methods ---

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
