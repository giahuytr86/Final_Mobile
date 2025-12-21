package com.testing.final_mobile.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.testing.final_mobile.R;
import com.example.final_mobile.data.model.Message;
import com.testing.final_mobile.ui.adapter.MessageAdapter;
import com.testing.final_mobile.ui.viewmodel.ChatViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageView btnSend, btnBack;
    private TextView tvChatName;

    private MessageAdapter adapter;
    private ChatViewModel viewModel;
    private String conversationId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Get Intent Data
        conversationId = getIntent().getStringExtra("conversationId");
        String chatName = getIntent().getStringExtra("chatName"); // Optional pass name

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 2. Initialize Views (IDs from your activity_chat.xml)
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        tvChatName = findViewById(R.id.tvChatName);

        if (chatName != null) tvChatName.setText(chatName);

        // 3. Setup RecyclerView
        adapter = new MessageAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Messages start from bottom
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);

        // 4. Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // 5. Listen for Messages
        if (conversationId != null) {
            viewModel.getMessages(conversationId).observe(this, messages -> {
                adapter.setList(messages);
                if (messages.size() > 0) {
                    rvChat.smoothScrollToPosition(messages.size() - 1);
                }
            });
        }

        // 6. Click Listeners
        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> finish());
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content) || conversationId == null) return;

        Message message = new Message(currentUserId, content, new Timestamp(new Date()));

        viewModel.sendMessage(conversationId, message);
        etMessage.setText("");
    }
}