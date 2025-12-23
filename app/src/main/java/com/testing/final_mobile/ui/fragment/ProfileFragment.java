package com.testing.final_mobile.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.databinding.FragmentProfileBinding;
import com.testing.final_mobile.databinding.LayoutProfileContentBinding;
import com.testing.final_mobile.ui.activity.PostDetailActivity;
import com.testing.final_mobile.ui.activity.SettingsActivity;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment implements PostAdapter.OnPostInteractionListener {

    private FragmentProfileBinding fragmentBinding; // Main binding for the fragment
    private LayoutProfileContentBinding contentBinding; // Binding for the included layout
    private ProfileViewModel viewModel;
    private PostAdapter postAdapter;
    private String currentUserId;

    private ActivityResultLauncher<String> mGetContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserId = FirebaseAuth.getInstance().getUid();

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                viewModel.updateAvatar(uri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentBinding = FragmentProfileBinding.inflate(inflater, container, false);
        contentBinding = fragmentBinding.profileContent; // Access the included layout
        return fragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUserId == null) {
            // Handle user not logged in case, maybe navigate to login screen
            return;
        }

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.init(currentUserId);

        setupRecyclerView();
        observeViewModel();
        setupClickListeners();
    }

    private void setupClickListeners() {
        contentBinding.ivProfileAvatar.setOnClickListener(v -> mGetContent.launch("image/*"));

        contentBinding.btnEditProfile.setOnClickListener(v -> {
            // Navigate to an EditProfileActivity or show a dialog
            Toast.makeText(getContext(), "Edit Profile functionality coming soon!", Toast.LENGTH_SHORT).show();
        });

        // The settings button is in the fragment's own layout, not the included one
        fragmentBinding.ivSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this);
        contentBinding.rvProfileContent.setLayoutManager(new LinearLayoutManager(getContext()));
        contentBinding.rvProfileContent.setAdapter(postAdapter);
    }

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), this::updateUi);
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                postAdapter.submitList(posts);
            }
        });
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                contentBinding.progressBar.setVisibility(View.VISIBLE);
            } else {
                contentBinding.progressBar.setVisibility(View.GONE);
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUi(User user) {
        if (user == null || !isAdded()) return;

        contentBinding.tvProfileName.setText(user.getUsername());
        // Make sure you have a unique handle or ID to display. Using UID for now.
        contentBinding.tvProfileId.setText("@" + user.getUid());
        contentBinding.tvBio.setText(user.getBio());

        Glide.with(this)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.placeholder_avatar) // Ensure you have this placeholder
                .circleCrop()
                .into(contentBinding.ivProfileAvatar);

        // On our own profile, show the "Edit Profile" button and hide the others.
        contentBinding.btnMessage.setVisibility(View.GONE);
        contentBinding.btnProfileCommand.setVisibility(View.GONE);
        contentBinding.btnEditProfile.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProfileClicked(String userId) {
        // This shouldn't be called from the user's own profile view in a way that navigates.
        // If it is, it might be a bug in the adapter's logic.
    }

    @Override
    public void onPostClicked(String postId) {
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_POST_ID, postId);
        startActivity(intent);
    }

    @Override
    public void onLikeClicked(String postId) {
        viewModel.toggleLike(postId);
    }

    @Override
    public void onCommentClicked(String postId) {
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
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
        // Implement options like delete or edit for the user's own posts.
        Toast.makeText(getContext(), "More options coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        contentBinding = null;
        fragmentBinding = null;
    }
}
