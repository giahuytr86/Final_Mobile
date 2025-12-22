package com.testing.final_mobile.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.testing.final_mobile.data.model.ChatMessage;
import com.testing.final_mobile.databinding.ItemMessageLeftBinding;
import com.testing.final_mobile.databinding.ItemMessageRightBinding;

public class ChatMessageAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatMessageAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            ItemMessageRightBinding binding = ItemMessageRightBinding.inflate(inflater, parent, false);
            return new SentMessageViewHolder(binding);
        } else {
            ItemMessageLeftBinding binding = ItemMessageLeftBinding.inflate(inflater, parent, false);
            return new ReceivedMessageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = getItem(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    // ViewHolder for sent messages (right side)
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageRightBinding binding;

        public SentMessageViewHolder(ItemMessageRightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message) {
            binding.tvMessageBody.setText(message.getMessage());
        }
    }

    // ViewHolder for received messages (left side)
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageLeftBinding binding;

        public ReceivedMessageViewHolder(ItemMessageLeftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message) {
            binding.tvMessageBody.setText(message.getMessage());
            // You can also load the sender's avatar here if you have the URL
        }
    }

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatMessage>() {
        @Override
        public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
            return oldItem.getMessageId().equals(newItem.getMessageId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
            return oldItem.getMessage().equals(newItem.getMessage());
        }
    };
}
