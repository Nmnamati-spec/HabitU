package com.habitu.app;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SENT     = 1;
    private static final int RECEIVED = 2;

    private final List<ChatMessage> messages;
    private final String currentUserId;

    public ChatMessageAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId) ? SENT : RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == SENT) {
            return new SentHolder(inf.inflate(R.layout.item_chat_message_sent, parent, false));
        }
        return new ReceivedHolder(inf.inflate(R.layout.item_chat_message_received, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        String time = msg.getTimestamp() != null
                ? new SimpleDateFormat("HH:mm", Locale.getDefault()).format(msg.getTimestamp().toDate())
                : "";

        if (holder instanceof SentHolder) {
            SentHolder h = (SentHolder) holder;
            h.tvText.setText(msg.getText());
            h.tvTime.setText(time);
        } else {
            ReceivedHolder h = (ReceivedHolder) holder;
            h.tvSenderName.setText(msg.getSenderName());
            h.tvText.setText(msg.getText());
            h.tvTime.setText(time);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class SentHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;
        SentHolder(View v) {
            super(v);
            tvText = v.findViewById(R.id.tvMessageText);
            tvTime = v.findViewById(R.id.tvTimestamp);
        }
    }

    static class ReceivedHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvText, tvTime;
        ReceivedHolder(View v) {
            super(v);
            tvSenderName = v.findViewById(R.id.tvSenderName);
            tvText       = v.findViewById(R.id.tvMessageText);
            tvTime       = v.findViewById(R.id.tvTimestamp);
        }
    }
}
