package com.testing.final_mobile.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.testing.final_mobile.databinding.FragmentSearchBinding;
import com.testing.final_mobile.ui.adapter.SearchViewPagerAdapter;
import com.testing.final_mobile.ui.viewmodel.SearchViewModel;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Use requireActivity() to scope the ViewModel to the Activity
        // This allows child fragments to access the same instance
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        setupViewPagerAndTabs();
        setupSearch();
    }

    private void setupViewPagerAndTabs() {
        SearchViewPagerAdapter adapter = new SearchViewPagerAdapter(getChildFragmentManager(), getLifecycle());
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 1) {
                tab.setText("Posts");
            } else {
                tab.setText("Users");
            }
        }).attach();
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchTerm = s.toString();
                // Trigger search for both types
                viewModel.searchUsers(searchTerm);
                viewModel.searchPosts(searchTerm);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
