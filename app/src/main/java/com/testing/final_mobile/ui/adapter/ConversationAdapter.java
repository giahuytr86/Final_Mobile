package com.testing.final_mobile.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.testing.final_mobile.R;
import com.testing.final_mobile.data.model.Conversation;
import com.testing.final_mobile.databinding.ItemConversationBinding;
import com.testing.final_mobile.utils.TimestampConverter;

public class ConversationAdapter extends ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClicked(Conversation conversation);
    }

    private final OnConversationClickListener listener;

    public ConversationAdapter(OnConversationClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Conversation> DIFF_CALLBACK = new DiffUtil.ItemCallback<Conversation>() {
        @Override
        public boolean areItemsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
            return oldItem.getConversationId().equals(newItem.getConversationId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
            return oldItem.getLastMessage().equals(newItem.getLastMessage()) &&
                   oldItem.getLastMessageTimestamp().equals(newItem.getLastMessageTimestamp());
        }
    };

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConversationBinding binding = ItemConversationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ConversationViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final ItemConversationBinding binding;
        private final OnConversationClickListener listener;

        public ConversationViewHolder(ItemConversationBinding binding, OnConversationClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        public void bind(Conversation conversation) {
            binding.tvUserName.setText(conversation.getOtherUserName());
            binding.tvLastMessage.setText(conversation.getLastMessage());

            if (conversation.getLastMessageTimestamp() != null) {
                binding.tvTimestamp.setText(TimestampConverter.getTimeAgo(conversation.getLastMessageTimestamp()));
            }

            Glide.with(itemView.getContext())
                    .load(conversation.getOtherUserAvatar())
                    .placeholder(R.drawable.placeholder_avatar)
                    .circleCrop()
                    .into(binding.ivUserAvatar);

            itemView.setOnClickListener(v -> listener.onConversationClicked(conversation));
        }
    }
}
