package com.testing.final_mobile.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.testing.final_mobile.databinding.AcitivityCreatePostBinding; // Corrected filename
import com.testing.final_mobile.ui.viewmodel.CreatePostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private AcitivityCreatePostBinding binding;
    private CreatePostViewModel viewModel;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.ivImagePreview.setImageURI(imageUri);
                    binding.ivImagePreview.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AcitivityCreatePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnClose.setOnClickListener(v -> finish());
        binding.btnPost.setOnClickListener(v -> {
            String content = binding.etContent.getText().toString().trim();
            if (!TextUtils.isEmpty(content) || imageUri != null) {
                viewModel.createPost(content, imageUri);
            } else {
                Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            selectImageLauncher.launch(intent);
        });
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            binding.btnPost.setEnabled(!isLoading);
            // You might want to add a ProgressBar to the layout and control it here
        });

        viewModel.postCreated.observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
