package com.testing.final_mobile.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.testing.final_mobile.databinding.ActivityChatBinding;
import com.testing.final_mobile.ui.adapter.ChatMessageAdapter;
import com.testing.final_mobile.ui.viewmodel.MessageViewModel;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_RECEIVER_ID = "EXTRA_RECEIVER_ID";
    public static final String EXTRA_RECEIVER_NAME = "EXTRA_RECEIVER_NAME";

    private ActivityChatBinding binding;
    private MessageViewModel viewModel;
    private ChatMessageAdapter adapter;
    private String receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiverId = getIntent().getStringExtra(EXTRA_RECEIVER_ID);
        String receiverName = getIntent().getStringExtra(EXTRA_RECEIVER_NAME);

        if (receiverId == null) {
            Toast.makeText(this, "Receiver ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        viewModel.init(receiverId);

        setupToolbar(receiverName);
        setupRecyclerView();
        observeViewModel();
        setupSendButton();
    }

    private void setupToolbar(String title) {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title != null ? title : "Chat");
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(this, messages -> {
            adapter.submitList(messages, () -> {
                // Scroll to the bottom to show the latest message
                if (adapter.getItemCount() > 0) {
                    binding.rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            });
        });

        viewModel.isMessageSent().observe(this, sent -> {
            if (sent) {
                binding.etMessage.setText("");
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> {
            String messageText = binding.etMessage.getText().toString();
            viewModel.sendMessage(messageText, receiverId);
        });
    }
}
