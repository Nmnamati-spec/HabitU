package com.habitu.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    List<DocumentSnapshot> posts;
    Context context;
    String userId;
    FirebaseFirestore db;
    OnCommentClickListener commentListener;

    // Emoji placeholders for posts with no image
    String[] emojis = {"🏃", "📚", "💪", "🧘", "🥗", "💧", "📖", "🌅", "🏋️", "🎯"};

    public interface OnCommentClickListener {
        void onCommentClick(String postId, TextView btnComment);
    }

    public PostAdapter(List<DocumentSnapshot> posts, Context context,
                       String userId, FirebaseFirestore db,
                       OnCommentClickListener listener) {
        this.posts           = posts;
        this.context         = context;
        this.userId          = userId;
        this.db              = db;
        this.commentListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        DocumentSnapshot doc = posts.get(position);

        String postId    = doc.getId();
        String userName  = doc.getString("userName");
        String caption   = doc.getString("caption");
        String habit     = doc.getString("habit");
        String imageUrl  = doc.getString("imageUrl");
        boolean discover = doc.getBoolean("discover") != null
                && doc.getBoolean("discover");
        long likes       = doc.getLong("likes") != null
                ? doc.getLong("likes") : 0;

        holder.tvHabitTag.setText(habit);
        holder.tvCaption.setText(caption);
        holder.tvPostUser.setText("👤 " + userName);
        holder.btnLike.setText("🤍 " + likes);

        // Show discover badge
        if (discover) {
            holder.tvDiscoverBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvDiscoverBadge.setVisibility(View.GONE);
        }

        // Load image or show emoji placeholder
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.imgPost.setVisibility(View.VISIBLE);
            holder.tvNoImageEmoji.setVisibility(View.GONE);
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.imgPost);
        } else {
            holder.imgPost.setVisibility(View.GONE);
            holder.tvNoImageEmoji.setVisibility(View.VISIBLE);
            // Pick emoji based on habit
            String emoji = getEmojiForHabit(habit);
            holder.tvNoImageEmoji.setText(emoji);
        }

        // Load comment count and preview
        db.collection("posts").document(postId)
                .collection("comments")
                .orderBy("timestamp",
                        com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(comments -> {
                    int count = comments.size();
                    holder.btnComment.setText("💬 " + count);
                    if (!comments.isEmpty()) {
                        String name    = comments.getDocuments()
                                .get(0).getString("userName");
                        String preview = comments.getDocuments()
                                .get(0).getString("text");
                        holder.tvCommentPreview.setText(name + ": " + preview);
                    } else {
                        holder.tvCommentPreview.setText("Be the first to comment...");
                    }
                });

        // Like button
        final long[] currentLikes = {likes};
        holder.btnLike.setOnClickListener(v -> {
            currentLikes[0]++;
            holder.btnLike.setText("❤️ " + currentLikes[0]);
            db.collection("posts").document(postId)
                    .update("likes", currentLikes[0]);
        });

        // Comment button and preview
        holder.btnComment.setOnClickListener(v ->
                commentListener.onCommentClick(postId, holder.btnComment));
        holder.layoutCommentPreview.setOnClickListener(v ->
                commentListener.onCommentClick(postId, holder.btnComment));
    }

    private String getEmojiForHabit(String habit) {
        if (habit == null) return "🌱";
        String h = habit.toLowerCase();
        if (h.contains("run"))   return "🏃";
        if (h.contains("study")) return "📚";
        if (h.contains("gym"))   return "💪";
        if (h.contains("meditat")) return "🧘";
        if (h.contains("nutrit") || h.contains("eat")) return "🥗";
        if (h.contains("water") || h.contains("hydrat")) return "💧";
        if (h.contains("read"))  return "📖";
        if (h.contains("sleep")) return "😴";
        return "🌱";
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPost;
        TextView tvNoImageEmoji, tvHabitTag, tvDiscoverBadge,
                tvCaption, tvPostUser, btnLike, btnComment,
                tvCommentPreview, tvViewAll;
        LinearLayout layoutCommentPreview;

        PostViewHolder(View v) {
            super(v);
            imgPost              = v.findViewById(R.id.imgPost);
            tvNoImageEmoji       = v.findViewById(R.id.tvNoImageEmoji);
            tvHabitTag           = v.findViewById(R.id.tvHabitTag);
            tvDiscoverBadge      = v.findViewById(R.id.tvDiscoverBadge);
            tvCaption            = v.findViewById(R.id.tvCaption);
            tvPostUser           = v.findViewById(R.id.tvPostUser);
            btnLike              = v.findViewById(R.id.btnLike);
            btnComment           = v.findViewById(R.id.btnComment);
            tvCommentPreview     = v.findViewById(R.id.tvCommentPreview);
            tvViewAll            = v.findViewById(R.id.tvViewAll);
            layoutCommentPreview = v.findViewById(R.id.layoutCommentPreview);
        }
    }
}