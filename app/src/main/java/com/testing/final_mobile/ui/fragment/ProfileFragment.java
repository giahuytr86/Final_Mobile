package com.testing.final_mobile.ui.fragment;

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
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.databinding.FragmentProfileBinding;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private PostAdapter postAdapter;
    private String profileUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This fragment always shows the current user's profile
        profileUserId = FirebaseAuth.getInstance().getUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        if (profileUserId != null) {
            viewModel.init(profileUserId);
        }

        setupRecyclerView();
        observeViewModel();
        // No need for setupClickListeners for the user's own profile actions like follow
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(postId -> viewModel.toggleLike(postId));
        binding.rvProfileContent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProfileContent.setAdapter(postAdapter);
    }

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), this::updateUi);

        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.submitList(posts);
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUi(User user) {
        if (user == null || !isAdded()) return;

        binding.tvProfileName.setText(user.getUsername());
        binding.tvProfileId.setText("@" + user.getUsername());
        binding.tvBio.setText(user.getBio());

        Glide.with(this)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.placeholder_avatar)
                .circleCrop()
                .into(binding.ivProfileAvatar);

        // Hide follow/message buttons as this is the user's own profile
        binding.btnMessage.setVisibility(View.GONE);
        binding.btnProfileCommand.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
