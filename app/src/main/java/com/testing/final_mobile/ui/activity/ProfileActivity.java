package com.testing.final_mobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.databinding.ActivityProfileBinding;
import com.testing.final_mobile.databinding.LayoutProfileContentBinding;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity implements PostAdapter.OnPostInteractionListener {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    private ActivityProfileBinding activityBinding; // For the toolbar
    private LayoutProfileContentBinding contentBinding; // For the main content
    private ProfileViewModel viewModel;
    private PostAdapter postAdapter;
    private String profileUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the container activity layout
        activityBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());
        // Get the binding for the included content
        contentBinding = activityBinding.profileContent;

        profileUserId = getIntent().getStringExtra(EXTRA_USER_ID);

        // The ProfileActivity is for viewing OTHER users' profiles.
        // If the user ID is the current user's, we redirect to HomeActivity which handles the user's own profile view.
        if (profileUserId == null || profileUserId.isEmpty() || profileUserId.equals(FirebaseAuth.getInstance().getUid())) {
            // Optional: You might want to show a message or just finish silently.
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.init(profileUserId);

        setupToolbar();
        setupRecyclerView();
        observeViewModel();
        setupClickListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(activityBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        activityBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this);
        contentBinding.rvProfileContent.setLayoutManager(new LinearLayoutManager(this));
        contentBinding.rvProfileContent.setAdapter(postAdapter);
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, this::updateUi);
        viewModel.getPosts().observe(this, posts -> postAdapter.submitList(posts));
        viewModel.isFollowing().observe(this, isFollowing -> {
            contentBinding.btnProfileCommand.setText(isFollowing ? "Unfollow" : "Follow");
        });
        viewModel.isLoading().observe(this, isLoading -> {
            contentBinding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUi(User user) {
        if (user == null) return;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(user.getUsername());
        }
        contentBinding.tvProfileName.setText(user.getUsername());
        contentBinding.tvProfileId.setText("@" + user.getUsername()); // Or a different field if you have a unique handle
        contentBinding.tvBio.setText(user.getBio());

        Glide.with(this)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.placeholder_avatar) // Ensure you have this drawable
                .circleCrop()
                .into(contentBinding.ivProfileAvatar);

        // This is a profile of another user, so we show buttons to interact.
        contentBinding.btnMessage.setVisibility(View.VISIBLE);
        contentBinding.btnProfileCommand.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        contentBinding.btnProfileCommand.setOnClickListener(v -> viewModel.toggleFollow());

        contentBinding.btnMessage.setOnClickListener(v -> {
            User user = viewModel.getUser().getValue(); // Get the current user object
            if (user == null) {
                Toast.makeText(this, "User data not available yet.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_RECEIVER_ID, user.getUid());
            intent.putExtra(ChatActivity.EXTRA_RECEIVER_NAME, user.getUsername());
            startActivity(intent);
        });
    }

    // --- PostAdapter.OnPostInteractionListener Implementation ---

    @Override
    public void onProfileClicked(String userId) {
        // Since we are already on a profile, we might not need to do anything here,
        // or we could refresh the activity for the new user ID if it's different.
    }

    @Override
    public void onPostClicked(String postId) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_POST_ID, postId);
        startActivity(intent);
    }

    @Override
    public void onLikeClicked(String postId) {
        viewModel.toggleLike(postId);
    }

    @Override
    public void onCommentClicked(String postId) {
        // This should probably go to PostDetailActivity as well
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_POST_ID, postId);
        startActivity(intent);
    }

    @Override
    public void onShareClicked(Post post) {
        if (post == null || post.getContent() == null) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, post.getContent());
        startActivity(Intent.createChooser(shareIntent, "Share post via"));
    }

    @Override
    public void onMoreClicked(Post post) {
        // Implement options for posts, e.g., report, etc.
        Toast.makeText(this, "Options for this post are not yet implemented.", Toast.LENGTH_SHORT).show();
    }
}
