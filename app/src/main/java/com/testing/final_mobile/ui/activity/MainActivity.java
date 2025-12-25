package com.testing.final_mobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.testing.final_mobile.R;
import com.testing.final_mobile.ui.fragment.HomeFragment;
import com.testing.final_mobile.ui.fragment.ProfileFragment;
import com.testing.final_mobile.ui.fragment.SearchFragment;
import com.testing.final_mobile.ui.fragment.ConversationFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Thiết lập sự kiện lắng nghe cho bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.navigation_add_post) {
                // Mở Activity tạo bài viết mới
                Intent intent = new Intent(MainActivity.this, CreatePostActivity.class);
                startActivity(intent);
                // Trả về false để không làm thay đổi trạng thái chọn (selection) của thanh menu
                return false; 
            } else if (itemId == R.id.navigation_notifications) {
                selectedFragment = new ConversationFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Load fragment mặc định (Trang chủ)
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
