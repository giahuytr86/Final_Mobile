package com.testing.final_mobile.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.testing.final_mobile.databinding.AcitivityCreatePostBinding;
import com.testing.final_mobile.ui.viewmodel.CreatePostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private AcitivityCreatePostBinding binding;
    private CreatePostViewModel viewModel;

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
            if (content.isEmpty()) {
                Toast.makeText(this, "What's happening?", Toast.LENGTH_SHORT).show();
                return;
            }
            // For now, we pass a null imageUrl. You can add image selection logic later.
            viewModel.createPost(content, null);
        });
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) {
                binding.btnPost.setEnabled(false);
                binding.btnPost.setText("POSTING...");
                // You could also show a ProgressBar here
            } else {
                binding.btnPost.setEnabled(true);
                binding.btnPost.setText("POST");
            }
        });

        viewModel.isPostCreated.observe(this, isCreated -> {
            if (isCreated) {
                Toast.makeText(this, "Posted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
