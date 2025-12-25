package com.testing.final_mobile.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.testing.final_mobile.data.model.User;
import com.testing.final_mobile.databinding.FragmentUserSearchResultsBinding;
import com.testing.final_mobile.ui.activity.ProfileActivity;
import com.testing.final_mobile.ui.adapter.UserAdapter;
import com.testing.final_mobile.ui.viewmodel.SearchViewModel;

public class UserSearchResultsFragment extends Fragment implements UserAdapter.OnUserClickListener {

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

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        // Pass 'this' as the listener to the adapter
        userAdapter = new UserAdapter(this);
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
    public void onUserClick(User user) {
        // When a user is clicked, open their profile
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        // Make sure ProfileActivity uses this same key to retrieve the ID
        intent.putExtra("EXTRA_USER_ID", user.getUid());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
