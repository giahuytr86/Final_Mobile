package com.testing.final_mobile.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.testing.final_mobile.databinding.FragmentUserSearchResultsBinding;
import com.testing.final_mobile.ui.adapter.UserAdapter;
import com.testing.final_mobile.ui.viewmodel.SearchViewModel;

public class UserSearchResultsFragment extends Fragment {

    private FragmentUserSearchResultsBinding binding;
    private SearchViewModel sharedViewModel;
    private UserAdapter userAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserSearchResultsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the ViewModel shared from the parent SearchFragment
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter();
        binding.rvUserResults.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUserResults.setAdapter(userAdapter);
    }

    private void observeViewModel() {
        sharedViewModel.userSearchResults.observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                userAdapter.submitList(users);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
