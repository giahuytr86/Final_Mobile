package com.testing.final_mobile.data.remote.core;

import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

public class StorageService {

    private static final String TAG = "StorageService";

    public interface OnImageUploadListener {
        void onSuccess(String downloadUrl);
        void onError(Exception e);
    }

    public void uploadImageAndGetDownloadUrl(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri == null) {
            listener.onError(new IllegalArgumentException("Image URI cannot be null."));
            return;
        }

        // Tải ảnh lên Cloudinary
        MediaManager.get().upload(imageUri)
                .option("folder", "final_mobile_assets") // Tên thư mục trên Cloudinary của bạn
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Cloudinary upload started: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Lấy link ảnh an toàn (https) trả về từ Cloudinary
                        String publicUrl = (String) resultData.get("secure_url");
                        Log.d(TAG, "Cloudinary upload success: " + publicUrl);
                        listener.onSuccess(publicUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                        listener.onError(new Exception(error.getDescription()));
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Cloudinary upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }
}
