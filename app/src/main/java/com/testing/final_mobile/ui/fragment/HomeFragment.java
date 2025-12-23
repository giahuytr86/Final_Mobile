package com.testing.final_mobile.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.databinding.FragmentHomeBinding;
import com.testing.final_mobile.ui.activity.CreatePostActivity;
import com.testing.final_mobile.ui.activity.PostDetailActivity;
import com.testing.final_mobile.ui.activity.ProfileActivity;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.HomeViewModel;

public class HomeFragment extends Fragment implements PostAdapter.OnPostInteractionListener {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private PostAdapter postAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        loadUserAvatar();
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this);
        binding.rvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFeed.setAdapter(postAdapter);
    }

    private void setupClickListeners() {
        binding.layoutCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                postAdapter.submitList(posts);
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()){
                Toast.makeText(getContext(), "Error: "+error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadUserAvatar() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && getContext() != null) {
            Glide.with(getContext())
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.placeholder_avatar)
                    .circleCrop()
                    .into(binding.ivMyAvatar);
        }
    }

    // --- Implementation of OnPostInteractionListener ---

    @Override
    public void onProfileClicked(String userId) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.EXTRA_USER_ID, userId);
        startActivity(intent);
    }

    @Override
    public void onPostClicked(String postId) {
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_POST_ID, postId);
        startActivity(intent);
    }

    @Override
    public void onLikeClicked(String postId) {
        viewModel.toggleLikeStatus(postId);
    }

    @Override
    public void onCommentClicked(String postId) {
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_POST_ID, postId);
        startActivity(intent);
    }

    @Override
    public void onShareClicked(Post post) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, post.getContent());
        startActivity(Intent.createChooser(shareIntent, "Share post via"));
    }

    @Override
    public void onMoreClicked(Post post) {
        Toast.makeText(getContext(), "More options coming soon!", Toast.LENGTH_SHORT).show();
    }
}
