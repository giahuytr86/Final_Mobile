package com.testing.final_mobile.data.remote;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.remote.core.FirestoreService;

import java.util.Collections;
import java.util.List;

public class PostRemoteDataSource {

    private final FirestoreService firestoreService;

    public interface OnPostsFetchedListener {
        void onPostsFetched(List<Post> posts);
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

    public interface OnPostsSearchedListener {
        void onPostsSearched(List<Post> posts);
        void onError(Exception e);
    }

    public PostRemoteDataSource(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public void createPost(Post post, OnPostCreatedListener listener) {
        firestoreService.addDocument("posts", post, task -> {
            if (task.isSuccessful()) {
                listener.onPostCreated(task.getResult());
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void fetchAllPosts(OnPostsFetchedListener listener) {
        Query query = firestoreService.getCollection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);
        firestoreService.getCollection(query, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    List<Post> posts = task.getResult().toObjects(Post.class);
                    listener.onPostsFetched(posts);
                } catch (Exception e) {
                    listener.onError(e);
                }
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void getPostsForUser(String userId, OnPostsFetchedListener listener) {
        if (userId == null) {
            listener.onPostsFetched(Collections.emptyList());
            return;
        }
        Query query = firestoreService.getCollection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
        firestoreService.getCollection(query, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    List<Post> posts = task.getResult().toObjects(Post.class);
                    listener.onPostsFetched(posts);
                } catch (Exception e) {
                    listener.onError(e);
                }
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void fetchPostById(String postId, OnPostFetchedListener listener) {
        DocumentReference docRef = firestoreService.getDocument("posts", postId);
        firestoreService.getDocument(docRef, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    Post post = task.getResult().toObject(Post.class);
                    listener.onPostFetched(post);
                } catch (Exception e) {
                    listener.onError(e);
                }
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void toggleLikeStatus(String postId, String userId, OnPostLikeUpdatedListener listener) {
        DocumentReference postRef = firestoreService.getDocument("posts", postId);
        firestoreService.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(postRef);
            Post post = snapshot.toObject(Post.class);
            if (post != null) {
                List<String> likes = post.getLikes();
                if (likes != null && likes.contains(userId)) {
                    transaction.update(postRef, "likes", FieldValue.arrayRemove(userId));
                } else {
                    transaction.update(postRef, "likes", FieldValue.arrayUnion(userId));
                }
            }
            return null;
        }, task -> {
            if (task.isSuccessful()) {
                listener.onPostLikeUpdated();
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void searchPosts(String searchTerm, OnPostsSearchedListener listener) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            listener.onPostsSearched(Collections.emptyList());
            return;
        }
        String lowercasedTerm = searchTerm.toLowerCase();
        Query query = firestoreService.getCollection("posts")
                .whereGreaterThanOrEqualTo("searchableContent", lowercasedTerm)
                .whereLessThanOrEqualTo("searchableContent", lowercasedTerm + '\uf8ff');
        firestoreService.getCollection(query, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    List<Post> posts = task.getResult().toObjects(Post.class);
                    listener.onPostsSearched(posts);
                } catch (Exception e) {
                    listener.onError(e);
                }
            } else {
                listener.onError(task.getException());
            }
        });
    }
}
