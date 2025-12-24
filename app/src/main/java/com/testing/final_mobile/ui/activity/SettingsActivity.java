package com.testing.final_mobile.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.remote.core.StorageService;
import com.testing.final_mobile.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private StorageService storageService;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadAvatar(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storageService = new StorageService();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupClickListeners();
        loadCurrentAvatar();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCurrentAvatar() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String avatarUrl = documentSnapshot.getString("avatarUrl");
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.placeholder_avatar)
                                .into(binding.ivAvatar);
                    }
                });
    }

    private void setupClickListeners() {
        // Nhấn vào avatar để đổi ảnh
        binding.ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile feature coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change Password feature coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void uploadAvatar(Uri imageUri) {
        // Hiển thị một feedback đơn giản (có thể thêm ProgressBar nếu muốn)
        Toast.makeText(this, "Updating avatar...", Toast.LENGTH_SHORT).show();
        binding.ivAvatar.setAlpha(0.5f); // Làm mờ ảnh để báo đang xử lý

        storageService.uploadImageAndGetDownloadUrl(imageUri, new StorageService.OnImageUploadListener() {
            @Override
            public void onSuccess(String downloadUrl) {
                String uid = auth.getUid();
                if (uid == null) return;

                // Cập nhật link mới vào Firestore
                db.collection("users").document(uid)
                        .update("avatarUrl", downloadUrl)
                        .addOnSuccessListener(aVoid -> {
                            binding.ivAvatar.setAlpha(1.0f);
                            Glide.with(SettingsActivity.this).load(downloadUrl).into(binding.ivAvatar);
                            Toast.makeText(SettingsActivity.this, "Avatar updated!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            binding.ivAvatar.setAlpha(1.0f);
                            Toast.makeText(SettingsActivity.this, "Failed to update database", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onError(Exception e) {
                binding.ivAvatar.setAlpha(1.0f);
                Toast.makeText(SettingsActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
