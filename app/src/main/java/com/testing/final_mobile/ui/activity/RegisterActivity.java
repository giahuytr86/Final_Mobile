package com.testing.final_mobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import com.testing.final_mobile.R;
import com.testing.final_mobile.ui.viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegEmail, etRegPassword, etRegConfirmPass;
    private AppCompatButton btnRegisterSubmit;
    private ProgressBar progressBar;
    private TextView tvLoginLink;

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViewModel();
        initViews();
        initEvents();
        observeViewModel();
    }

    private void initViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    private void initViews() {
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPass = findViewById(R.id.etRegConfirmPass);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        progressBar = findViewById(R.id.progressBar);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void initEvents() {
        btnRegisterSubmit.setOnClickListener(v -> performRegistration());
        tvLoginLink.setOnClickListener(v -> {
            // Navigate back to LoginActivity
            finish();
        });
    }

    private void observeViewModel() {
        authViewModel.user.observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show();
                // Navigate to Login screen after successful registration
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        authViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnRegisterSubmit.setEnabled(!isLoading);
        });
    }

    private void performRegistration() {
        String username = etRegUsername.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPass = etRegConfirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etRegUsername.setError("Username is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etRegEmail.setError("Email is required");
            return;
        }
        if (password.length() < 6) {
            etRegPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPass)) {
            etRegConfirmPass.setError("Passwords do not match");
            return;
        }

        authViewModel.register(email, password, username);
    }
}
