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

import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.databinding.FragmentPostSearchResultsBinding;
import com.testing.final_mobile.ui.activity.PostDetailActivity;
import com.testing.final_mobile.ui.activity.ProfileActivity;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.SearchViewModel;

public class PostSearchResultsFragment extends Fragment implements PostAdapter.OnPostInteractionListener {

    private FragmentPostSearchResultsBinding binding;
    private SearchViewModel sharedViewModel;
    private PostAdapter postAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostSearchResultsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        // Initialize adapter with `this` as the listener
        postAdapter = new PostAdapter(this);
        binding.rvPostResults.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPostResults.setAdapter(postAdapter);
    }

    private void observeViewModel() {
        sharedViewModel.postSearchResults.observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                postAdapter.submitList(posts);
                binding.tvNoResults.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                binding.tvNoResults.setVisibility(View.VISIBLE);
            }
        });
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
        sharedViewModel.toggleLikeStatus(postId);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
