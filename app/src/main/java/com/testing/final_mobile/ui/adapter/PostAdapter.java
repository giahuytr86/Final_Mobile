package com.testing.final_mobile.ui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

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
import com.testing.final_mobile.ui.activity.ProfileActivity;
import com.testing.final_mobile.utils.TimestampConverter;

import java.util.Objects;

public class PostAdapter extends ListAdapter<Post, PostAdapter.PostViewHolder> {

    // Interface duy nhất chứa tất cả các sự kiện tương tác
    public interface OnPostInteractionListener {
        void onLikeClicked(String postId);
        void onDeleteClicked(Post post);
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

            String currentUserId = FirebaseAuth.getInstance().getUid();
            
            // Hiển thị nút btnMore nếu là bài viết của chính mình
            if (currentUserId != null && currentUserId.equals(post.getUserId())) {
                binding.btnMore.setVisibility(View.VISIBLE);
                binding.btnMore.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenu().add("Xóa bài viết");
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getTitle().equals("Xóa bài viết")) {
                            if (listener != null) {
                                listener.onDeleteClicked(post);
                            }
                        }
                        return true;
                    });
                    popupMenu.show();
                });
            } else {
                binding.btnMore.setVisibility(View.GONE);
            }

            // Xử lý hiển thị icon Like
            if (currentUserId != null && post.getLikes().contains(currentUserId)) {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                binding.ivLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline);
                binding.ivLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_gray));
            }

            // Xử lý ảnh bài viết
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                binding.ivPostImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(post.getImageUrl())
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image)
                        .into(binding.ivPostImage);
            } else {
                binding.ivPostImage.setVisibility(View.GONE);
            }

            // Sự kiện click vào Profile
            View.OnClickListener profileClickListener = v -> {
                Intent intent = new Intent(itemView.getContext(), ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, post.getUserId());
                itemView.getContext().startActivity(intent);
            };

            binding.ivUserAvatar.setOnClickListener(profileClickListener);
            binding.tvUserName.setOnClickListener(profileClickListener);

            // Sự kiện Like
            binding.btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClicked(post.getId());
                }
            });

            // Sự kiện Share
            binding.btnShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, post.getContent());
                itemView.getContext().startActivity(Intent.createChooser(shareIntent, "Share post via"));
            });

            // Sự kiện click vào chi tiết bài viết
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getId());
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
