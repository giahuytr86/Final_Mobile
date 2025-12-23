package com.testing.final_mobile.data.remote;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.ArrayList;
import java.util.List;

public class PostRemoteDataSource {

    private static final String TAG = "PostRemoteDataSource";
    private static final String POST_COLLECTION = "posts";

    private final FirestoreService firestoreService;

    //<editor-fold desc="Interfaces">
    public interface OnPostsFetchedListener {
        void onPostsFetched(List<Post> posts);
        void onError(Exception e);
    }

    public interface OnPostsSearchedListener {
        void onPostsSearched(List<Post> posts);
        void onError(Exception e);
    }

    public interface OnPostFetchedListener {
        void onPostFetched(Post post);
        void onError(Exception e);
    }

    public interface OnPostCreatedListener {
        void onPostCreated(DocumentReference documentReference);
        void onError(Exception e);
    }

    public interface OnPostLikeUpdatedListener {
        void onPostLikeUpdated();
        void onError(Exception e);
    }
    //</editor-fold>

    public PostRemoteDataSource(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public DocumentReference getNewPostReference() {
        return firestoreService.getCollection(POST_COLLECTION).document();
    }

    public void searchPosts(String searchTerm, OnPostsSearchedListener listener) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            listener.onPostsSearched(new ArrayList<>());
            return;
        }

        String lowercasedTerm = searchTerm.toLowerCase();

        Query query = firestoreService.getCollection(POST_COLLECTION)
                .orderBy("searchableContent")
                .startAt(lowercasedTerm)
                .endAt(lowercasedTerm + '\uf8ff')
                .limit(20);

        firestoreService.getCollection(query, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Post> posts = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Post post = doc.toObject(Post.class);
                    post.setId(doc.getId()); // Corrected method name
                    posts.add(post);
                }
                listener.onPostsSearched(posts);
            } else {
                Log.e(TAG, "Error searching posts", task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
    }

    public void toggleLikeStatus(String postId, String userId, OnPostLikeUpdatedListener listener) {
        DocumentReference postRef = firestoreService.getDocument(POST_COLLECTION, postId);

        Transaction.Function<Void> updateFunction = transaction -> {
            Post post = transaction.get(postRef).toObject(Post.class);
            if (post == null) {
                throw new FirebaseFirestoreException("Post not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            List<String> likes = post.getLikes(); // Correct type
            if (likes.contains(userId)) {
                likes.remove(userId);
            } else {
                likes.add(userId);
            }
            // The like count is now derived, so we only update the list.
            transaction.update(postRef, "likes", likes);
            return null;
        };

        firestoreService.runTransaction(updateFunction, (OnCompleteListener<Void>) task -> {
            if (task.isSuccessful()) {
                listener.onPostLikeUpdated();
            } else {
                Log.e(TAG, "Like transaction failed", task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
    }

    public void createPost(Post newPost, OnPostCreatedListener listener) {
        // Use the specific document reference to ensure the ID is what we set in the repository
        firestoreService.getDocument(POST_COLLECTION, newPost.getId()).set(newPost).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // The result of a set operation is void, so we pass the reference we already have.
                listener.onPostCreated(firestoreService.getDocument(POST_COLLECTION, newPost.getId()));
            } else {
                Log.e(TAG, "Error creating post", task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
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
                        post.setId(doc.getId()); // Corrected method name
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
                        post.setId(task.getResult().getId()); // Corrected method name
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

    public void fetchPostsByUserId(String userId, OnPostsFetchedListener listener) {
        Query query = firestoreService.getCollection(POST_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        firestoreService.getCollection(query, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Post> posts = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Post post = document.toObject(Post.class);
                    post.setId(document.getId());
                    posts.add(post);
                }
                listener.onPostsFetched(posts);
            } else {
                Log.e(TAG, "Error fetching user posts", task.getException());
                if (task.getException() != null) {
                    listener.onError(task.getException());
                }
            }
        });
    }
}
