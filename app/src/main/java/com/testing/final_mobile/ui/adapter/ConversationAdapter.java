package com.testing.final_mobile.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.testing.final_mobile.R;
import com.example.final_mobile.data.model.Conversation;
import com.testing.final_mobile.ui.activity.ChatActivity;
import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private Context context;
    private List<Conversation> list;

    public ConversationAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }

    public void setList(List<Conversation> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = list.get(position);

        // Ideally, you fetch the other user's name here based on participantIds
        holder.tvName.setText("User (ID: " + conversation.getConversationId().substring(0,4) + ")");
        holder.tvLastMsg.setText(conversation.getLastMessage());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("conversationId", conversation.getConversationId());
            // intent.putExtra("otherUserName", ...); // Pass name if you have it
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMsg, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_conversation_name);
            tvLastMsg = itemView.findViewById(R.id.tv_conversation_last_msg);
            tvTime = itemView.findViewById(R.id.tv_conversation_time);
        }
    }
}