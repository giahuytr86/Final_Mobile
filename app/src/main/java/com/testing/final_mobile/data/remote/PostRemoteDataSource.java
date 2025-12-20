package com.testing.final_mobile.data.remote;

import android.util.Log;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.ArrayList;
import java.util.List;

public class PostRemoteDataSource {

    private static final String TAG = "PostRemoteDataSource";
    private static final String POST_COLLECTION = "posts";

    private final FirestoreService firestoreService;

    public interface OnPostsFetchedListener {
        void onPostsFetched(List<Post> posts);
        void onError(Exception e);
    }

    public interface OnPostFetchedListener {
        void onPostFetched(Post post);
        void onError(Exception e);
    }

    public PostRemoteDataSource(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public void fetchAllPosts(OnPostsFetchedListener listener) {
        Query query = firestoreService.getCollection(POST_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        firestoreService.getCollection(query, task -> {
            if (task.isSuccessful()) {
                List<Post> postList = new ArrayList<>();
                QuerySnapshot snapshots = task.getResult();
                if (snapshots != null) {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Post post = doc.toObject(Post.class);
                        post.setPostId(doc.getId());
                        postList.add(post);
                    }
                }
                listener.onPostsFetched(postList);
            } else {
                Log.e(TAG, "Error fetching posts from server", task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
    }

    public void fetchPostById(String postId, OnPostFetchedListener listener) {
        firestoreService.getDocument(firestoreService.getDocument(POST_COLLECTION, postId), task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().exists()) {
                    Post post = task.getResult().toObject(Post.class);
                    if (post != null) {
                        post.setPostId(task.getResult().getId());
                        listener.onPostFetched(post);
                    }
                } else {
                    listener.onError(new Exception("Post not found"));
                }
            } else {
                Log.e(TAG, "Error fetching post " + postId + " from server", task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
    }
}
