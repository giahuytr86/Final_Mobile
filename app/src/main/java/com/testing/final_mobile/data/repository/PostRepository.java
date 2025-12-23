package com.testing.final_mobile.data.repository;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

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

import java.util.List;

public class PostRepository {

    private static final String TAG = "PostRepository";

    private final PostDao postDao;
    private final PostRemoteDataSource remoteDataSource;
    private final StorageService storageService;
    private final Application application;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public interface OnPostCreatedListener {
        void onPostCreated();
        void onError(Exception e);
    }

    public interface OnPostLikedListener {
        void onPostLiked();
        void onError(Exception e);
    }

    public interface OnPostsSearchedListener {
        void onPostsSearched(List<Post> posts);
        void onError(Exception e);
    }

    public PostRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.postDao = database.postDao();
        this.remoteDataSource = new PostRemoteDataSource(new FirestoreService());
        this.storageService = new StorageService();
        this.application = application;
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void createPost(String content, @Nullable Uri imageUri, OnPostCreatedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError(new Exception("User not logged in."));
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
                    if (user != null) {
                        createPostInFirestore(content, imageUrl, user, listener);
                    } else {
                        listener.onError(new Exception("Could not retrieve user details to create post. User document not found in Firestore."));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    private void createPostInFirestore(String content, @Nullable String imageUrl, User user, OnPostCreatedListener listener) {
        Post newPost = new Post();
        newPost.setUserId(user.getUid());
        newPost.setUsername(user.getUsername());
        newPost.setAvatarUrl(user.getAvatarUrl());
        newPost.setContent(content);
        newPost.setImageUrl(imageUrl);

        remoteDataSource.createPost(newPost, new PostRemoteDataSource.OnPostCreatedListener() {
            @Override
            public void onPostCreated(DocumentReference documentReference) {
                documentReference.get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Post createdPost = snapshot.toObject(Post.class);
                        if (createdPost != null) {
                            AppDatabase.databaseWriteExecutor.execute(() -> postDao.insert(createdPost));
                            listener.onPostCreated();
                        } else {
                            listener.onError(new Exception("Failed to parse created post from server."));
                        }
                    } else {
                        listener.onError(new Exception("Failed to fetch post back from server after creation."));
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Post was created, but failed to fetch it back. A full refresh will fix it later.", e);
                    listener.onPostCreated();
                });
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public void searchPosts(String searchTerm, OnPostsSearchedListener listener) {
        remoteDataSource.searchPosts(searchTerm, new PostRemoteDataSource.OnPostsSearchedListener() {
            @Override
            public void onPostsSearched(List<Post> posts) {
                listener.onPostsSearched(posts);
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public void toggleLikeStatus(String postId, OnPostLikedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }
        remoteDataSource.toggleLikeStatus(postId, currentUser.getUid(), new PostRemoteDataSource.OnPostLikeUpdatedListener() {
            @Override
            public void onPostLikeUpdated() {
                listener.onPostLiked();
                refreshPostFromServer(postId);
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public LiveData<List<Post>> getAllPosts() {
        // The ViewModel should be responsible for refreshing data.
        // Removing automatic refresh to prevent race conditions and data loss.
        return postDao.getAllPosts();
    }

    public LiveData<List<Post>> getPostsForUser(String userId) {
        // The ViewModel should be responsible for refreshing data.
        return postDao.getPostsForUser(userId);
    }

    public LiveData<Post> getPostById(String postId) {
        // The ViewModel should be responsible for refreshing data.
        return postDao.getPostById(postId);
    }

    public void refreshPostsFromServer() {
        if (!isNetworkAvailable()) return;
        remoteDataSource.fetchAllPosts(new PostRemoteDataSource.OnPostsFetchedListener() {
            @Override
            public void onPostsFetched(List<Post> posts) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    postDao.deleteAllAndInsertAll(posts);
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching all posts from server", e);
            }
        });
    }

    public void refreshPostsForUserFromServer(String userId) {
        if (!isNetworkAvailable()) return;
        remoteDataSource.getPostsForUser(userId, new PostRemoteDataSource.OnPostsFetchedListener() {
            @Override
            public void onPostsFetched(List<Post> posts) {
                AppDatabase.databaseWriteExecutor.execute(() -> postDao.insertAll(posts));
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching posts for user " + userId, e);
            }
        });
    }

    public void refreshPostFromServer(String postId) {
        if (!isNetworkAvailable()) return;
        remoteDataSource.fetchPostById(postId, new PostRemoteDataSource.OnPostFetchedListener() {
            @Override
            public void onPostFetched(Post post) {
                if (post != null) {
                    AppDatabase.databaseWriteExecutor.execute(() -> postDao.insert(post));
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching post " + postId + " from server", e);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
