package com.testing.final_mobile.data.repository;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.testing.final_mobile.data.local.AppDatabase;
import com.testing.final_mobile.data.local.PostDao;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.remote.FirebaseManager;

import java.util.Collections;
import java.util.List;

public class PostRepository {

    private static final String TAG = "PostRepository";

    private final PostDao postDao;
    private final FirebaseManager remoteDataSource;
    private final Application application;

    public PostRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.postDao = database.postDao();
        this.remoteDataSource = new FirebaseManager(); // The single point of contact for remote data
        this.application = application;
    }

    // --- Public API for ViewModels ---

    public LiveData<List<Post>> getAllPosts() {
        refreshPostsFromServer();
        return postDao.getAllPosts();
    }

    public LiveData<Post> getPostById(String postId) {
        refreshPostFromServer(postId);
        return postDao.getPostById(postId);
    }

    // --- Data Refreshing Logic ---

    private void refreshPostsFromServer() {
        if (!isNetworkAvailable()) return;

        remoteDataSource.fetchAllPosts(new FirebaseManager.OnPostsFetchedListener() {
            @Override
            public void onPostsFetched(List<Post> posts) {
                // Save the fetched data to the local database
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    postDao.insertAll(posts);
                    Log.d(TAG, "Successfully refreshed " + posts.size() + " posts from server.");
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing posts from server", e);
            }
        });
    }

    private void refreshPostFromServer(String postId) {
        if (!isNetworkAvailable()) return;

        remoteDataSource.fetchPostById(postId, new FirebaseManager.OnPostFetchedListener() {
            @Override
            public void onPostFetched(Post post) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    postDao.insertAll(Collections.singletonList(post));
                    Log.d(TAG, "Successfully refreshed post " + postId + " from server.");
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing post " + postId + " from server", e);
            }
        });
    }

    // --- Utility Methods ---

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
