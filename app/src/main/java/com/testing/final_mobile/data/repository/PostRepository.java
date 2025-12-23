package com.testing.final_mobile.data.repository;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.testing.final_mobile.data.local.AppDatabase;
import com.testing.final_mobile.data.local.PostDao;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.data.remote.PostRemoteDataSource;
import com.testing.final_mobile.data.remote.core.FirestoreService;
import com.testing.final_mobile.data.remote.core.StorageService;

import java.util.Collections;
import java.util.List;

public class PostRepository {

    private static final String TAG = "PostRepository";

    private final PostDao postDao;
    private final PostRemoteDataSource remoteDataSource;
    private final StorageService storageService;
    private final Application application;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public interface OnPostCreatedListener {
        void onPostCreated();
        void onError(Exception e);
    }

    public interface OnPostLikedListener {
        void onPostLiked();
        void onError(Exception e);
    }

    public PostRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.postDao = database.postDao();
        this.remoteDataSource = new PostRemoteDataSource(new FirestoreService());
        this.storageService = new StorageService(); // Initialize StorageService
        this.application = application;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void createPost(String content, @Nullable Uri imageUri, OnPostCreatedListener listener) {
        if (!isNetworkAvailable()) {
            listener.onError(new Exception("No internet connection"));
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        if (imageUri != null) {
            storageService.uploadImageAndGetDownloadUrl(imageUri, new StorageService.OnImageUploadListener() {
                @Override
                public void onSuccess(String downloadUrl) {
                    fetchUserAndCreatePost(content, downloadUrl, currentUser, listener);
                }

                @Override
                public void onError(Exception e) {
                    listener.onError(e);
                }
            });
        } else {
            fetchUserAndCreatePost(content, null, currentUser, listener);
        }
    }

    private void fetchUserAndCreatePost(String content, @Nullable String imageUrl, FirebaseUser currentUser, OnPostCreatedListener listener) {
        firestore.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    String username = (user != null && user.getUsername() != null) ? user.getUsername() : currentUser.getEmail();
                    String avatarUrl = (user != null) ? user.getAvatarUrl() : null;
                    createPostInFirestore(content, imageUrl, currentUser.getUid(), username, avatarUrl, listener);
                })
                .addOnFailureListener(listener::onError);
    }

    private void createPostInFirestore(String content, @Nullable String imageUrl, String userId, String username, String avatarUrl, OnPostCreatedListener listener) {
        DocumentReference newPostRef = remoteDataSource.getNewPostReference();

        Post newPost = new Post();
        newPost.setId(newPostRef.getId());
        newPost.setUserId(userId);
        newPost.setUsername(username);
        newPost.setAvatarUrl(avatarUrl);
        newPost.setContent(content);
        newPost.setImageUrl(imageUrl);

        remoteDataSource.createPost(newPost, new PostRemoteDataSource.OnPostCreatedListener() {
            @Override
            public void onPostCreated(DocumentReference documentReference) {
                refreshPostFromServer(documentReference.getId());
                listener.onPostCreated();
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public void searchPosts(String searchTerm, MutableLiveData<List<Post>> searchResults, MutableLiveData<String> error) {
        remoteDataSource.searchPosts(searchTerm, new PostRemoteDataSource.OnPostsSearchedListener() {
            @Override
            public void onPostsSearched(List<Post> posts) {
                searchResults.postValue(posts);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public void toggleLikeStatus(String postId, OnPostLikedListener listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null || !isNetworkAvailable()) {
            listener.onError(new Exception("Not logged in or no network"));
            return;
        }

        remoteDataSource.toggleLikeStatus(postId, currentUserId, new PostRemoteDataSource.OnPostLikeUpdatedListener() {
            @Override
            public void onPostLikeUpdated() {
                refreshPostFromServer(postId);
                listener.onPostLiked();
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public LiveData<List<Post>> getAllPosts() {
        refreshPostsFromServer();
        return postDao.getAllPosts();
    }

    public LiveData<Post> getPostById(String postId) {
        refreshPostFromServer(postId);
        return postDao.getPostById(postId);
    }

    public LiveData<List<Post>> getPostsByUserId(String userId) {
        refreshUserPostsFromServer(userId); // Optional: refresh data from remote
        return postDao.getPostsByUserId(userId);
    }

    private void refreshPostsFromServer() {
        if (!isNetworkAvailable()) return;

        remoteDataSource.fetchAllPosts(new PostRemoteDataSource.OnPostsFetchedListener() {
            @Override
            public void onPostsFetched(List<Post> posts) {
                AppDatabase.databaseWriteExecutor.execute(() -> postDao.insertAll(posts));
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing posts from server", e);
            }
        });
    }

    private void refreshPostFromServer(String postId) {
        if (!isNetworkAvailable()) return;

        remoteDataSource.fetchPostById(postId, new PostRemoteDataSource.OnPostFetchedListener() {
            @Override
            public void onPostFetched(Post post) {
                AppDatabase.databaseWriteExecutor.execute(() -> postDao.insertAll(Collections.singletonList(post)));
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing post " + postId + " from server", e);
            }
        });
    }

    private void refreshUserPostsFromServer(String userId) {
        if (!isNetworkAvailable()) return;
        remoteDataSource.fetchPostsByUserId(userId, new PostRemoteDataSource.OnPostsFetchedListener() {
            @Override
            public void onPostsFetched(List<Post> posts) {
                AppDatabase.databaseWriteExecutor.execute(() -> postDao.insertAll(posts));
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing user posts from server", e);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
