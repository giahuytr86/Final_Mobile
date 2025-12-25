package com.testing.final_mobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.databinding.ActivityProfileBinding;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    private ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private PostAdapter postAdapter;
    private String userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (userId == null) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.init(userId);

        setupToolbar();
        setupRecyclerView();
        observeViewModel();
        setupClickListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        // Sửa lỗi: Triển khai Interface đầy đủ thay vì dùng lambda
        postAdapter = new PostAdapter(new PostAdapter.OnPostInteractionListener() {
            @Override
            public void onLikeClicked(String postId) {
                viewModel.toggleLike(postId);
            }

            @Override
            public void onDeleteClicked(Post post) {
                showDeleteConfirmationDialog(post);
            }
        });
        
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPosts.setAdapter(postAdapter);
    }

    private void showDeleteConfirmationDialog(Post post) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deletePost(post.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, user -> {
            this.currentUser = user;
            updateUi(user);
        });
        viewModel.getPosts().observe(this, posts -> postAdapter.submitList(posts));
        viewModel.isFollowing().observe(this, isFollowing -> {
            binding.btnFollow.setText(isFollowing ? "Unfollow" : "Follow");
        });
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUi(User user) {
        if (user == null) return;

        binding.tvUsername.setText(user.getUsername());
        binding.tvBio.setText(user.getBio());

        Glide.with(this)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.placeholder_avatar)
                .circleCrop()
                .into(binding.ivAvatar);
    }

    private void setupClickListeners() {
        binding.btnFollow.setOnClickListener(v -> viewModel.toggleFollow());
        binding.btnMessage.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_RECEIVER_ID, currentUser.getUid());
                intent.putExtra(ChatActivity.EXTRA_RECEIVER_NAME, currentUser.getUsername());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Cannot get user info to start chat", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
