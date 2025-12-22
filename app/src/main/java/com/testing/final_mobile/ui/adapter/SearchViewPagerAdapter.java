package com.testing.final_mobile.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.testing.final_mobile.ui.fragment.PostSearchResultsFragment;
import com.testing.final_mobile.ui.fragment.UserSearchResultsFragment;

public class SearchViewPagerAdapter extends FragmentStateAdapter {

    public SearchViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new PostSearchResultsFragment();
        }
        return new UserSearchResultsFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs: Users and Posts
    }
}
