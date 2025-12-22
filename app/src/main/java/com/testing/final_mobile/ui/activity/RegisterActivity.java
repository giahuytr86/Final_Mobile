package com.testing.final_mobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.testing.final_mobile.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegId, etRegEmail, etRegPassword, etRegConfirmPass;
    private AppCompatButton btnRegisterSubmit;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        initEvents();
    }

    private void initViews() {
        etRegId = findViewById(R.id.etRegId);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPass = findViewById(R.id.etRegConfirmPass);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        progressBar = findViewById(R.id.progressBar); // Đảm bảo ID này có trong XML
    }

    private void initEvents() {
        btnRegisterSubmit.setOnClickListener(v -> performRegistration());
    }

    private void performRegistration() {
        String username = etRegId.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPass = etRegConfirmPass.getText().toString().trim();

        // 1. Kiểm tra dữ liệu đầu vào (Validation)
        if (TextUtils.isEmpty(username)) {
            etRegId.setError("Vui lòng nhập Username");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etRegEmail.setError("Vui lòng nhập Email");
            return;
        }
        if (password.length() < 6) {
            etRegPassword.setError("Mật khẩu phải từ 6 ký tự");
            return;
        }
        if (!password.equals(confirmPass)) {
            etRegConfirmPass.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        // 2. Hiển thị Loading
        progressBar.setVisibility(View.VISIBLE);
        btnRegisterSubmit.setEnabled(false);

        // 3. Đăng ký tài khoản trên Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký Auth thành công, lưu thông tin bổ sung vào Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToFirestore(user.getUid(), username, email);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegisterSubmit.setEnabled(true);
                        Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String username, String email) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("role", "user"); // Mặc định role là user

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    // Chuyển sang màn hình Login hoặc Main
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegisterSubmit.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
