package com.testing.final_mobile.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.databinding.ItemUserBinding;

import java.util.Objects;

public class UserAdapter extends ListAdapter<User, UserAdapter.UserViewHolder> {

    public UserAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUid().equals(newItem.getUid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            // Corrected to use new getters and be null-safe
            return Objects.equals(oldItem.getUsername(), newItem.getUsername()) &&
                   Objects.equals(oldItem.getAvatarUrl(), newItem.getAvatarUrl());
        }
    };

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            // Corrected to use getUsername()
            binding.tvUserName.setText(user.getUsername());
            if (user.getUsername() != null) {
                binding.tvUserHandle.setText("@" + user.getUsername().toLowerCase().replaceAll("\\s", ""));
            } else {
                binding.tvUserHandle.setText("@user");
            }

            // Corrected to use getAvatarUrl()
            Glide.with(itemView.getContext())
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.placeholder_avatar)
                    .circleCrop()
                    .into(binding.ivUserAvatar);

            // TODO: Add click listener for the item or the follow button
        }
    }
}
