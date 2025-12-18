package com.testing.final_mobile.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.testing.final_mobile.R;
import com.testing.final_mobile.ui.viewmodel.CreatePostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etContent;
    private TextView btnPost;
    private ImageView btnClose;

    private CreatePostViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_create_post);

        // 1. Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);

        etContent = findViewById(R.id.etContent);
        btnPost = findViewById(R.id.btnPost);
        btnClose = findViewById(R.id.btnClose);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        // 3. Observe LiveData to restore content on configuration change
        viewModel.getPostContent().observe(this, content -> {
            if (!content.equals(etContent.getText().toString())) {
                etContent.setText(content);
                etContent.setSelection(content.length()); // Move cursor to the end
            }
        });

        // 5. Observe for successful post
        viewModel.postSuccessful.observe(this, successful -> {
            if (successful) {
                Toast.makeText(this, "Posted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // 5. Observe for errors
        viewModel.error.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                btnPost.setEnabled(true); // Re-enable post button on error
            }
        });
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> finish());

        // 2. Add TextWatcher to save content to ViewModel
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setPostContent(s.toString());
            }
        });

        // 4. Call ViewModel to create the post
        btnPost.setOnClickListener(v -> {
            btnPost.setEnabled(false); // Prevent multiple clicks
            viewModel.createPost();
        });
    }
}
