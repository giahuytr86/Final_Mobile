package com.testing.final_mobile.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.databinding.ItemPostBinding;
import com.testing.final_mobile.utils.TimestampConverter;

public class PostAdapter extends ListAdapter<Post, PostAdapter.PostViewHolder> {

    public interface OnPostInteractionListener {
        void onProfileClicked(String userId);
        void onPostClicked(String postId);
        void onLikeClicked(String postId);
        void onCommentClicked(String postId);
        void onShareClicked(Post post);
        void onMoreClicked(Post post);
    }

    private final OnPostInteractionListener listener;

    public PostAdapter(OnPostInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostBinding binding;
        private final OnPostInteractionListener listener;

        PostViewHolder(ItemPostBinding binding, OnPostInteractionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Post post) {
            if (post == null) return;

            // Bind data
            binding.tvUserName.setText(post.getUsername());
            binding.tvPostContent.setText(post.getContent());
            binding.tvLikeCount.setText(String.valueOf(post.getLikes().size()));
            binding.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

            if (post.getTimestamp() != null) {
                binding.tvPostDate.setText(TimestampConverter.getTimeAgo(post.getTimestamp()));
            }

            Glide.with(itemView.getContext())
                    .load(post.getAvatarUrl())
                    .placeholder(R.drawable.placeholder_avatar)
                    .circleCrop()
                    .into(binding.ivUserAvatar);

            // Like button state
            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId != null && post.getLikes().contains(currentUserId)) {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                binding.ivLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline);
                binding.ivLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_gray));
            }

            // --- Set up all click listeners to delegate to the interface ---

            // Click on user profile
            Runnable profileClickAction = () -> listener.onProfileClicked(post.getUserId());
            binding.ivUserAvatar.setOnClickListener(v -> profileClickAction.run());
            binding.tvUserName.setOnClickListener(v -> profileClickAction.run());

            // Click on post content to go to detail
            itemView.setOnClickListener(v -> listener.onPostClicked(post.getId()));

            // Click on action buttons
            binding.btnLike.setOnClickListener(v -> listener.onLikeClicked(post.getId()));
            binding.btnComment.setOnClickListener(v -> listener.onCommentClicked(post.getId()));
            binding.btnShare.setOnClickListener(v -> listener.onShareClicked(post));
            binding.btnMore.setOnClickListener(v -> listener.onMoreClicked(post));
        }
    }

    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {
        @Override
        public boolean areItemsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.equals(newItem);
        }
    };
}
