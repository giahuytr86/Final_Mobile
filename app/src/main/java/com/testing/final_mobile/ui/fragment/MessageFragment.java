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
import androidx.recyclerview.widget.RecyclerView;
import com.testing.final_mobile.R;
import com.testing.final_mobile.ui.adapter.ConversationAdapter;
import com.testing.final_mobile.ui.viewmodel.ChatViewModel;

public class MessageFragment extends Fragment {

    private RecyclerView rvMessages;
    private ConversationAdapter adapter;
    private ChatViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMessages = view.findViewById(R.id.rvMessages); // ID from your fragment_message.xml
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ConversationAdapter(getContext());
        rvMessages.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Listen to conversation list updates
        viewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            adapter.setList(conversations);
        });
    }
}