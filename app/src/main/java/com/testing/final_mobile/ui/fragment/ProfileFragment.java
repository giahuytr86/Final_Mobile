package com.testing.final_mobile.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.databinding.FragmentProfileBinding;
import com.testing.final_mobile.ui.activity.SettingsActivity;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment implements PostAdapter.OnPostInteractionListener {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private PostAdapter postAdapter;

    private String currentUserId;
    private String profileUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getUid();
        profileUserId = currentUserId; // Default to showing the current user's profile

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupRecyclerView();
        observeViewModel();
        setupClickListeners();

        if (profileUserId != null) {
            viewModel.loadUser(profileUserId);
        }
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this);
        binding.rvProfileContent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProfileContent.setAdapter(postAdapter);
        binding.rvProfileContent.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> {
            if(profileUserId.equals(currentUserId)){
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            } else {
                requireActivity().finish();
            }
        });

        binding.tabPosts.setOnClickListener(v -> selectTab(binding.tabPosts));
        binding.tabPhotos.setOnClickListener(v -> selectTab(binding.tabPhotos));
        binding.tabAbout.setOnClickListener(v -> selectTab(binding.tabAbout));

        binding.btnProfileCommand.setOnClickListener(v -> {
            User user = viewModel.user.getValue();
            if (user == null) return;

            if (profileUserId.equals(currentUserId)) {
                // Open settings on Edit Profile click
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            } else {
                if (user.getFollowers().contains(currentUserId)) {
                    viewModel.unfollowUser(profileUserId);
                } else {
                    viewModel.followUser(profileUserId);
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.user.observe(getViewLifecycleOwner(), this::updateUi);

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUi(User user) {
        if (user == null || getContext() == null) return;

        binding.tvProfileName.setText(user.getUsername());
        binding.tvProfileId.setText("@" + user.getUsername());
        binding.tvBio.setText(user.getBio());

        Glide.with(this).load(user.getAvatarUrl()).placeholder(R.drawable.placeholder_avatar).into(binding.ivProfileAvatar);

        binding.tvFollowingCount.setText(String.valueOf(user.getFollowing().size()));
        binding.tvFollowerCount.setText(String.valueOf(user.getFollowers().size()));

        if (profileUserId.equals(currentUserId)) {
            binding.btnProfileCommand.setText("Edit Profile");
            binding.btnMessage.setVisibility(View.GONE);
            binding.btnBack.setImageResource(R.drawable.ic_settings); // Change back button to settings icon
        } else {
            binding.btnMessage.setVisibility(View.VISIBLE);
            binding.btnBack.setImageResource(R.drawable.ic_arrow_left);
            if (user.getFollowers().contains(currentUserId)) {
                binding.btnProfileCommand.setText("Unfollow");
                binding.btnProfileCommand.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_gray));
                binding.btnProfileCommand.setTextColor(Color.BLACK);
            } else {
                binding.btnProfileCommand.setText("Follow");
                binding.btnProfileCommand.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                binding.btnProfileCommand.setTextColor(Color.WHITE);
            }
        }
    }

    private void selectTab(View selectedTab) {
        // Reset all tabs to default state
        resetTabState(binding.tabPosts, binding.tvTabPosts, binding.indicatorPosts);
        resetTabState(binding.tabPhotos, binding.tvTabPhotos, binding.indicatorPhotos);
        resetTabState(binding.tabAbout, binding.tvTabAbout, binding.indicatorAbout);

        // Set the selected tab to active state
        if (selectedTab.getId() == R.id.tabPosts) {
            activateTab(binding.tvTabPosts, binding.indicatorPosts);
            binding.rvProfileContent.setVisibility(View.VISIBLE);
            binding.layoutAbout.setVisibility(View.GONE);
        } else if (selectedTab.getId() == R.id.tabPhotos) {
            activateTab(binding.tvTabPhotos, binding.indicatorPhotos);
            // TODO: Handle Photos tab click
            binding.rvProfileContent.setVisibility(View.VISIBLE); // For now, still show posts
            binding.layoutAbout.setVisibility(View.GONE);
        } else if (selectedTab.getId() == R.id.tabAbout) {
            activateTab(binding.tvTabAbout, binding.indicatorAbout);
            binding.rvProfileContent.setVisibility(View.GONE);
            binding.layoutAbout.setVisibility(View.VISIBLE);
        }
    }

    private void resetTabState(View tab, TextView textView, View indicator) {
        if (getContext() == null) return;
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.text_gray));
        textView.setTypeface(null, android.graphics.Typeface.NORMAL);
        indicator.setBackgroundColor(Color.TRANSPARENT);
    }

    private void activateTab(TextView textView, View indicator) {
        if (getContext() == null) return;
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        indicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
    }

    @Override
    public void onLikeClicked(String postId) {
        viewModel.toggleLikeStatus(postId);
    }
}
