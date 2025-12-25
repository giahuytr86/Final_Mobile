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

import com.testing.final_mobile.databinding.FragmentConversationsBinding;
import com.testing.final_mobile.ui.activity.ChatActivity;
import com.testing.final_mobile.ui.adapter.ConversationAdapter;
import com.testing.final_mobile.ui.viewmodel.ConversationViewModel;

public class ConversationFragment extends Fragment {

    private FragmentConversationsBinding binding;
    private ConversationViewModel viewModel;
    private ConversationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConversationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ConversationViewModel.class);

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ConversationAdapter(conversation -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_RECEIVER_ID, conversation.getOtherUserId());
            intent.putExtra(ChatActivity.EXTRA_RECEIVER_NAME, conversation.getOtherUserName());
            startActivity(intent);
        });
        binding.rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConversations.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            if (conversations != null && !conversations.isEmpty()) {
                adapter.submitList(conversations);
                binding.rvConversations.setVisibility(View.VISIBLE);
                binding.tvEmptyState.setVisibility(View.GONE);
            } else {
                binding.rvConversations.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
