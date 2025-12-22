package com.testing.final_mobile.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Comment;
import com.testing.final_mobile.databinding.ItemCommentBinding;
import com.testing.final_mobile.utils.TimestampConverter;

import java.util.Objects;

public class CommentAdapter extends ListAdapter<Comment, CommentAdapter.CommentViewHolder> {

    public interface OnCommentInteractionListener {
        void onReplyClicked(Comment comment);
        // onLikeClicked is removed
    }

    private final OnCommentInteractionListener listener;

    public CommentAdapter(OnCommentInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Comment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            // Simplified the comparison as likes are removed
            return Objects.equals(oldItem.getContent(), newItem.getContent());
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
            binding.tvUserName.setText(comment.getUsername());
            binding.tvCommentContent.setText(comment.getContent());

            // Hide like-related views
            binding.tvLikeCount.setVisibility(View.GONE);
            binding.btnLikeComment.setVisibility(View.GONE);

            if (comment.getTimestamp() != null) {
                binding.tvTime.setText(TimestampConverter.getTimeAgo(comment.getTimestamp()));
            }

            Glide.with(itemView.getContext())
                    .load(comment.getAvatarUrl())
                    .placeholder(R.drawable.placeholder_avatar)
                    .circleCrop()
                    .into(binding.ivUserAvatar);

            if (comment.getParentCommentId() != null) {
                binding.viewReplyIndent.setVisibility(View.VISIBLE);
            } else {
                binding.viewReplyIndent.setVisibility(View.GONE);
            }

            binding.btnReply.setOnClickListener(v -> listener.onReplyClicked(comment));
        }
    }
}
