package com.testing.final_mobile.data.remote;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.testing.final_mobile.data.model.Post;

import java.util.ArrayList;
import java.util.List;

public class FirebaseManager {

    private static final String TAG = "FirebaseManager";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    // Listener for when posts are fetched
    public interface OnPostsFetchedListener {
        void onPostsFetched(List<Post> posts);
        void onError(Exception e);
    }

    // Listener for when a single post is fetched
    public interface OnPostFetchedListener {
        void onPostFetched(Post post);
        void onError(Exception e);
    }

    public FirebaseManager() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void fetchAllPosts(OnPostsFetchedListener listener) {
        firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> postList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        post.setPostId(doc.getId());
                        postList.add(post);
                    }
                    listener.onPostsFetched(postList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching posts from server", e);
                    listener.onError(e);
                });
    }

    public void fetchPostById(String postId, OnPostFetchedListener listener) {
        firestore.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(documentSnapshot.getId());
                            listener.onPostFetched(post);
                        }
                    } else {
                        listener.onError(new Exception("Post not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching post " + postId + " from server", e);
                    listener.onError(e);
                });
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}
