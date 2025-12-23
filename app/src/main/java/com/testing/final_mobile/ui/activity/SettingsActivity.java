package com.testing.final_mobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.testing.final_mobile.databinding.ActivitySettingsBinding;
import com.testing.final_mobile.ui.viewmodel.AuthViewModel;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        setupClickListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile coming soon!", Toast.LENGTH_SHORT).show();
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change Password coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Delegate logout action to ViewModel
        binding.btnLogout.setOnClickListener(v -> authViewModel.logout());
    }

    private void observeViewModel() {
        authViewModel.loggedOutEvent.observe(this, isLoggedOut -> {
            if (isLoggedOut) {
                navigateToLogin();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
