package com.testing.final_mobile.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.databinding.FragmentProfileBinding;
import com.testing.final_mobile.ui.activity.SettingsActivity;
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
        // Fragment này luôn hiển thị profile của người dùng hiện tại
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

        binding.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        // Sửa lỗi: Triển khai đầy đủ interface thay vì dùng lambda
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
        
        binding.rvProfileContent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProfileContent.setAdapter(postAdapter);
    }

    private void showDeleteConfirmationDialog(Post post) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deletePost(post.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), this::updateUi);

        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.submitList(posts);
        });

//        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
//            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
//        });

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

//        // Ẩn nút message/follow vì đây là trang cá nhân của chính mình
//        binding.btnMessage.setVisibility(View.GONE);
//        binding.btnProfileCommand.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
