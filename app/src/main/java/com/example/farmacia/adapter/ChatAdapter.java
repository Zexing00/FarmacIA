package com.example.farmacia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmacia.R;
import com.example.farmacia.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messages;

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT = 1;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSender() == ChatMessage.SENDER_USER ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            View v = inf.inflate(R.layout.item_chat_user, parent, false);
            return new UserVH(v);
        } else {
            View v = inf.inflate(R.layout.item_chat_bot, parent, false);
            return new BotVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = messages.get(position);
        if (holder instanceof UserVH) {
            ((UserVH) holder).tv.setText(m.getText());
        } else if (holder instanceof BotVH) {
            ((BotVH) holder).tv.setText(m.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView tv;
        UserVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvMsgUser);
        }
    }

    static class BotVH extends RecyclerView.ViewHolder {
        TextView tv;
        BotVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvMsgBot);
        }
    }
}
