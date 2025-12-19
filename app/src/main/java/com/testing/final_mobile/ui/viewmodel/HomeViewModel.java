package com.testing.final_mobile.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.testing.final_mobile.data.model.Post;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final FirebaseFirestore db;
    private final MutableLiveData<List<Post>> _posts = new MutableLiveData<>();
    public LiveData<List<Post>> posts = _posts;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        fetchPosts();
    }

    private void fetchPosts() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        _error.setValue("Listen failed: " + e.getMessage());
                        return;
                    }

                    if (snapshots != null) {
                        List<Post> postList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Post post = doc.toObject(Post.class);
                            post.setPostId(doc.getId()); // Manually set the document ID
                            postList.add(post);
                        }
                        _posts.setValue(postList);
                    } else {
                         _error.setValue("No data found");
                    }
                });
    }
}
