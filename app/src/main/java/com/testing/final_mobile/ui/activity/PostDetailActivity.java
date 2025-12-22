package com.testing.final_mobile.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.databinding.ActivityPostDetailBinding;
import com.testing.final_mobile.ui.adapter.CommentAdapter;
import com.testing.final_mobile.ui.adapter.PostAdapter;
import com.testing.final_mobile.ui.viewmodel.CommentViewModel;
import com.testing.final_mobile.ui.viewmodel.PostDetailViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity implements CommentAdapter.OnCommentInteractionListener {

    public static final String EXTRA_POST_ID = "EXTRA_POST_ID";
    private ActivityPostDetailBinding binding;
    private PostDetailViewModel postViewModel;
    private CommentViewModel commentViewModel;
    private PostAdapter postAdapter;
    private CommentAdapter commentAdapter;
    private String postId;

    private Comment replyingToComment = null; // To hold the comment being replied to

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null) {
            Toast.makeText(this, "Post ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViewModels();
        setupRecyclerViews();
        setupClickListeners();
        observeViewModels();

        postViewModel.fetchPost(postId);
    }

    private void setupViewModels() {
        postViewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);
        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);
    }

    private void setupRecyclerViews() {
        postAdapter = new PostAdapter(null); // No listener needed for post in detail view
        binding.rvPostContent.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPostContent.setAdapter(postAdapter);

        commentAdapter = new CommentAdapter(this);
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);
        binding.rvComments.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSendComment.setOnClickListener(v -> {
            String content = binding.etComment.getText().toString().trim();
            if (!content.isEmpty()) {
                String parentId = (replyingToComment != null) ? replyingToComment.getCommentId() : null;
                commentViewModel.addComment(postId, content, parentId);
            }
        });

        binding.btnCancelReply.setOnClickListener(v -> cancelReply());
    }

    private void observeViewModels() {
        postViewModel.post.observe(this, post -> {
            if (post != null) {
                postAdapter.submitList(Collections.singletonList(post));
            }
        });

        postViewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Post error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        commentViewModel.getCommentsForPost(postId).observe(this, comments -> {
            List<Comment> sortedComments = processComments(comments);
            commentAdapter.submitList(sortedComments);
        });

        commentViewModel.isLoading.observe(this, isLoading -> {
            binding.btnSendComment.setEnabled(!isLoading);
        });

        commentViewModel.commentAdded.observe(this, isAdded -> {
            if (isAdded) {
                binding.etComment.setText("");
                cancelReply();
                Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show();
            }
        });

        commentViewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Comment error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onReplyClicked(Comment comment) {
        replyingToComment = comment;
        binding.tvReplyingTo.setText("Replying to " + comment.getUserName());
        binding.layoutReplyBanner.setVisibility(View.VISIBLE);
        binding.etComment.requestFocus();
    }

    @Override
    public void onLikeClicked(Comment comment) {
        commentViewModel.toggleLikeStatus(postId, comment.getCommentId());
    }

    private void cancelReply() {
        replyingToComment = null;
        binding.layoutReplyBanner.setVisibility(View.GONE);
    }

    private List<Comment> processComments(List<Comment> flatList) {
        if (flatList == null) return new ArrayList<>();

        Map<String, List<Comment>> repliesMap = new HashMap<>();
        List<Comment> topLevelComments = new ArrayList<>();

        for (Comment comment : flatList) {
            if (comment.getParentCommentId() != null) {
                List<Comment> replies = repliesMap.getOrDefault(comment.getParentCommentId(), new ArrayList<>());
                replies.add(comment);
                repliesMap.put(comment.getParentCommentId(), replies);
            } else {
                topLevelComments.add(comment);
            }
        }

        List<Comment> sortedList = new ArrayList<>();
        for (Comment topComment : topLevelComments) {
            sortedList.add(topComment);
            List<Comment> replies = repliesMap.get(topComment.getCommentId());
            if (replies != null) {
                sortedList.addAll(replies);
            }
        }
        return sortedList;
    }
}
