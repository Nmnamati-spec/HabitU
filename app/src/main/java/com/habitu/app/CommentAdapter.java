package com.habitu.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    List<Map<String, Object>> comments;

    public CommentAdapter(List<Map<String, Object>> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> comment = comments.get(position);
        holder.tvName.setText((String) comment.get("userName"));
        holder.tvText.setText((String) comment.get("text"));
        holder.tvTime.setText("Just now");
    }

    @Override
    public int getItemCount() { return comments.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvText, tvTime;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvCommentName);
            tvText = v.findViewById(R.id.tvCommentText);
            tvTime = v.findViewById(R.id.tvCommentTime);
        }
    }
}
