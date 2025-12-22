package com.testing.final_mobile.ui.adapter;

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
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.databinding.ItemCommentBinding;
import com.testing.final_mobile.utils.TimestampConverter;

public class CommentAdapter extends ListAdapter<Comment, CommentAdapter.CommentViewHolder> {

    public interface OnCommentInteractionListener {
        void onReplyClicked(Comment comment);
        void onLikeClicked(Comment comment);
    }

    private final OnCommentInteractionListener listener;

    public CommentAdapter(OnCommentInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Comment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getCommentId().equals(newItem.getCommentId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getContent().equals(newItem.getContent()) &&
                   oldItem.getLikeCount() == newItem.getLikeCount() &&
                   oldItem.getLikes().equals(newItem.getLikes());
        }
    };

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = getItem(position);
        holder.bind(comment);
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;
        private final OnCommentInteractionListener listener;

        public CommentViewHolder(ItemCommentBinding binding, OnCommentInteractionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        public void bind(Comment comment) {
            binding.tvUserName.setText(comment.getUserName());
            binding.tvCommentContent.setText(comment.getContent());
            binding.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));

            if (comment.getCreatedAt() != null) {
                binding.tvTime.setText(TimestampConverter.getTimeAgo(comment.getCreatedAt()));
            }

            Glide.with(itemView.getContext())
                    .load(comment.getUserAvatarUrl())
                    .placeholder(R.drawable.placeholder_avatar)
                    .circleCrop()
                    .into(binding.ivUserAvatar);

            if (comment.getParentCommentId() != null) {
                binding.viewReplyIndent.setVisibility(View.VISIBLE);
            } else {
                binding.viewReplyIndent.setVisibility(View.GONE);
            }

            // Update Like button UI
            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId != null && comment.isLikedBy(currentUserId)) {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                binding.ivLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline);
                binding.ivLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_gray));
            }

            // --- Setup Click Listeners ---
            binding.btnReply.setOnClickListener(v -> listener.onReplyClicked(comment));
            binding.btnLikeComment.setOnClickListener(v -> listener.onLikeClicked(comment));
        }
    }
}
