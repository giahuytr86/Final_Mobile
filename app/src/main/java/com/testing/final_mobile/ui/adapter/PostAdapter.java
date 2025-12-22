package com.testing.final_mobile.ui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
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
import com.testing.final_mobile.ui.activity.PostDetailActivity;
import com.testing.final_mobile.utils.TimestampConverter;

import java.util.Objects;

public class PostAdapter extends ListAdapter<Post, PostAdapter.PostViewHolder> {

    public interface OnPostInteractionListener {
        void onLikeClicked(String postId);
    }

    private final OnPostInteractionListener listener;

    public PostAdapter(OnPostInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
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

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = getItem(position);
        if (post != null) {
            holder.bind(post);
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostBinding binding;
        private final OnPostInteractionListener listener;

        public PostViewHolder(ItemPostBinding binding, OnPostInteractionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        public void bind(Post post) {
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

            // The logic for showing the post image has been removed.

            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId != null && post.getLikes().contains(currentUserId)) {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                binding.ivLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline);
                binding.ivLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_gray));
            }

            binding.btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClicked(post.getId());
                }
            });

            binding.btnShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, post.getContent());
                itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Share post via"));
            });

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getId());
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
