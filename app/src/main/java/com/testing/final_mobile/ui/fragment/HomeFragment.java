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
import com.testing.final_mobile.databinding.FragmentHomeBinding;
import com.testing.final_mobile.ui.activity.CreatePostActivity;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.HomeViewModel;

// Implement the listener interface
public class HomeFragment extends Fragment implements PostAdapter.OnPostInteractionListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private PostAdapter postAdapter;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Pass 'this' as the listener
        postAdapter = new PostAdapter(this);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.rvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFeed.setAdapter(postAdapter);

        setupClickListeners();
        observeViewModel();
        updateUserAvatar();
    }

    private void setupClickListeners() {
        binding.layoutCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        homeViewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                postAdapter.submitList(posts);
            }
        });

        homeViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserAvatar() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .into(binding.ivMyAvatar);
        }
    }

    /**
     * This method is called from the PostAdapter when the like button is clicked.
     * @param postId The ID of the post that was liked.
     */
    @Override
    public void onLikeClicked(String postId) {
        homeViewModel.toggleLike(postId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
