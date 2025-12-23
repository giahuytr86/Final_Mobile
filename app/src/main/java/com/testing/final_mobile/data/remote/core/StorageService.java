package com.testing.final_mobile.data.remote.core;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class StorageService {

    private static final String TAG = "StorageService";
    private final FirebaseStorage storage;

    public interface OnImageUploadListener {
        void onSuccess(String downloadUrl);
        void onError(Exception e);
    }

    public StorageService() {
        this.storage = FirebaseStorage.getInstance();
    }

    public void uploadImageAndGetDownloadUrl(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri == null) {
            listener.onError(new IllegalArgumentException("Image URI cannot be null."));
            return;
        }

        String fileName = "images/" + UUID.randomUUID().toString();
        StorageReference fileRef = storage.getReference().child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> listener.onSuccess(uri.toString())) // Corrected this line
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to get download URL", e);
                            listener.onError(e);
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed", e);
                    listener.onError(e);
                });
    }
}
