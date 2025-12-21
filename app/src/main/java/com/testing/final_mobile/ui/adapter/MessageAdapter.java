package com.testing.final_mobile.ui.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.testing.final_mobile.R;
import com.example.final_mobile.data.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<Message> list;
    private String currentUserId;

    public MessageAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void setList(List<Message> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = list.get(position);
        holder.tvMessage.setText(message.getMessage());
    }

    @Override
    public int getItemCount() { return list.size(); }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getSenderId().equals(currentUserId)) {
            return MSG_TYPE_RIGHT;
        }
        return MSG_TYPE_LEFT;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage); // ID from your XML
        }
    }
}
