package com.testing.final_mobile.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Post;
import com.testing.final_mobile.utils.TimestampConverter;

public class PostAdapter extends ListAdapter<Post, PostAdapter.PostViewHolder> {

    public PostAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {
        @Override
        public boolean areItemsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getPostId().equals(newItem.getPostId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getContent().equals(newItem.getContent()) &&
                   oldItem.getLikeCount() == newItem.getLikeCount() &&
                   oldItem.getCommentCount() == newItem.getCommentCount();
        }
    };

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = getItem(position);
        holder.bind(post);
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivUserAvatar, ivPostImage;
        private final TextView tvUserName, tvPostDate, tvPostContent, tvLikeCount, tvCommentCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPostDate = itemView.findViewById(R.id.tvPostDate);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
        }

        public void bind(Post post) {
            tvUserName.setText(post.getUserName());
            tvPostContent.setText(post.getContent());
            tvLikeCount.setText(String.valueOf(post.getLikeCount()));
            tvCommentCount.setText(String.valueOf(post.getCommentCount()));
            
            // Format and display timestamp
            if (post.getTimestamp() != null) {
                tvPostDate.setText(TimestampConverter.getTimeAgo(post.getTimestamp()));
            }

            // Load user avatar
            Glide.with(itemView.getContext())
                    .load(post.getUserAvatarUrl())
                    .placeholder(R.drawable.placeholder_avatar)
                    .circleCrop()
                    .into(ivUserAvatar);

            // Load post image if it exists
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                ivPostImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(post.getImageUrl())
                        .into(ivPostImage);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }
        }
    }
}
