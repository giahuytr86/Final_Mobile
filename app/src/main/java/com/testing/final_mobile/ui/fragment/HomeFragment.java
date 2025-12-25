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
        setupSwipeRefresh(); // Gọi hàm xử lý refresh
        setupClickListeners();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserAvatar();
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this);
        binding.rvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFeed.setAdapter(postAdapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            // Gọi lại hàm lấy dữ liệu để đảm bảo danh sách được cập nhật
            viewModel.getAllPosts();

            // Tắt hiệu ứng xoay sau 1 giây (tạo cảm giác mượt mà như Facebook)
            binding.swipeRefreshLayout.postDelayed(() -> {
                if (binding != null) {
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
        });
    }

    private void setupClickListeners() {
        binding.layoutCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDeleteClicked(Post post) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (post.getUserId().equals(currentUserId)) {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Xóa bài viết")
                    .setMessage("Bạn có chắc chắn muốn xóa bài viết này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        viewModel.deletePost(post.getId());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            Toast.makeText(getContext(), "Bạn không có quyền xóa bài viết này", Toast.LENGTH_SHORT).show();
        }
    }

    private void observeViewModel() {
        viewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                postAdapter.submitList(posts);
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserAvatar() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null && getContext() != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (isAdded() && binding != null) {
                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            Glide.with(this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.placeholder_avatar)
                                    .circleCrop()
                                    .into(binding.ivMyAvatar);
                        }
                    });
        }
    }

    @Override
    public void onLikeClicked(String postId) {
        viewModel.toggleLikeStatus(postId);
    }
}
