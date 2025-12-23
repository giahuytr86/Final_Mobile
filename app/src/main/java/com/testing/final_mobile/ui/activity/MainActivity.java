package com.testing.final_mobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.testing.final_mobile.R;
import com.testing.final_mobile.ui.fragment.ConversationFragment;
import com.testing.final_mobile.ui.fragment.HomeFragment;
import com.testing.final_mobile.ui.fragment.ProfileFragment;
import com.testing.final_mobile.ui.fragment.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private final FragmentManager fm = getSupportFragmentManager();
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment searchFragment = new SearchFragment();
    private final Fragment conversationFragment = new ConversationFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private Fragment active = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Setup all fragments initially
        fm.beginTransaction().add(R.id.fragment_container, profileFragment, "4").hide(profileFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, conversationFragment, "3").hide(conversationFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, searchFragment, "2").hide(searchFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, homeFragment, "1").commit();

    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_home) {
            fm.beginTransaction().hide(active).show(homeFragment).commit();
            active = homeFragment;
            return true;
        } else if (itemId == R.id.navigation_search) {
            fm.beginTransaction().hide(active).show(searchFragment).commit();
            active = searchFragment;
            return true;
        } else if (itemId == R.id.navigation_add_post) {
            startActivity(new Intent(this, CreatePostActivity.class));
            // Return false so that the item is not selected, and the user stays on the current fragment.
            return false;
        } else if (itemId == R.id.navigation_notifications) { // This ID maps to the "Message" item now
            fm.beginTransaction().hide(active).show(conversationFragment).commit();
            active = conversationFragment;
            return true;
        } else if (itemId == R.id.navigation_profile) {
            fm.beginTransaction().hide(active).show(profileFragment).commit();
            active = profileFragment;
            return true;
        }

        return false;
    };
}
