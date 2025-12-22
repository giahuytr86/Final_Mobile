package com.testing.final_mobile.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.testing.final_mobile.databinding.ActivityChatBinding;
import com.testing.final_mobile.ui.adapter.ChatMessageAdapter;
import com.testing.final_mobile.ui.viewmodel.ChatViewModel;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONVERSATION_ID = "EXTRA_CONVERSATION_ID";
    public static final String EXTRA_OTHER_USER_ID = "EXTRA_OTHER_USER_ID";

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private ChatMessageAdapter adapter;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        if (conversationId == null) {
            Toast.makeText(this, "Conversation ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSend.setOnClickListener(v -> {
            String messageText = binding.etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                viewModel.sendMessage(conversationId, messageText);
                binding.etMessage.setText("");
            }
        });
    }

    private void observeViewModel() {
        viewModel.getMessagesForConversation(conversationId).observe(this, messages -> {
            if (messages != null) {
                adapter.submitList(messages);
                binding.rvMessages.scrollToPosition(messages.size() - 1);
            }
        });

        viewModel.isSending.observe(this, isSending -> {
            binding.btnSend.setEnabled(!isSending);
        });

        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
