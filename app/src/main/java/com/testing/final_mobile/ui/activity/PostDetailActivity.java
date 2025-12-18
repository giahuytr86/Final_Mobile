package com.testing.final_mobile.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.databinding.ActivityPostDetailBinding;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.PostDetailViewModel;

import java.util.Collections;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "EXTRA_POST_ID";
    private ActivityPostDetailBinding binding;
    private PostDetailViewModel viewModel;
    private PostAdapter postAdapter; // Re-use PostAdapter for the single post view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        String postId = getIntent().getStringExtra(EXTRA_POST_ID);
        viewModel.fetchPost(postId);
    }

    private void setupRecyclerView() {
        // We re-use the PostAdapter. We will submit a list containing only one item.
        postAdapter = new PostAdapter();
        binding.rvPostDetail.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPostDetail.setAdapter(postAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        viewModel.post.observe(this, post -> {
            if (post != null) {
                // The adapter expects a list, so we wrap our single post in a list
                postAdapter.submitList(Collections.singletonList(post));
                // You could also update a specific Post Detail header here if the layout was different
            }
        });

        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
