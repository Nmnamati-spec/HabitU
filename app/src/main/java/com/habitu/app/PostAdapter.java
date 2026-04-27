package com.habitu.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    List<DocumentSnapshot> posts;
    Context context;
    String userId;
    FirebaseFirestore db;
    OnCommentClickListener commentListener;

    public interface OnCommentClickListener {
        void onCommentClick(String postId, TextView tvCommentCount);
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

        String postId   = doc.getId();
        String userName = doc.getString("userName");
        String caption  = doc.getString("caption");
        String habit    = doc.getString("habit");
        String imageUrl = doc.getString("imageUrl");
        boolean isVideo  = doc.getBoolean("isVideo") != null && doc.getBoolean("isVideo");
        boolean discover = doc.getBoolean("discover") != null && doc.getBoolean("discover");
        long likes = doc.getLong("likes") != null ? doc.getLong("likes") : 0;

        holder.tvHabitTag.setText(habit);
        holder.tvCaption.setText(caption);
        holder.tvPostUser.setText(userName);
        holder.tvLikeCount.setText(String.valueOf(likes));

        // Discover badge
        holder.tvDiscoverBadge.setVisibility(discover ? View.VISIBLE : View.GONE);

        // Show video, image, or habit icon depending on post type
        if (isVideo && imageUrl != null && !imageUrl.isEmpty()) {
            holder.frameVideoPost.setVisibility(View.VISIBLE);
            holder.imgPost.setVisibility(View.GONE);
            holder.frameHabitIcon.setVisibility(View.GONE);
            final String videoUrl = imageUrl;
            holder.frameVideoPost.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(videoUrl), "video/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            });
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.imgPost.setVisibility(View.VISIBLE);
            holder.frameHabitIcon.setVisibility(View.GONE);
            holder.frameVideoPost.setVisibility(View.GONE);
            Glide.with(context).load(imageUrl).into(holder.imgPost);
        } else {
            holder.imgPost.setVisibility(View.GONE);
            holder.frameHabitIcon.setVisibility(View.VISIBLE);
            holder.frameVideoPost.setVisibility(View.GONE);
            holder.imgHabitIcon.setImageResource(HabitIconMapper.getIcon(habit));
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
                    holder.tvCommentCount.setText(String.valueOf(count));
                    if (!comments.isEmpty()) {
                        String name    = comments.getDocuments().get(0).getString("userName");
                        String preview = comments.getDocuments().get(0).getString("text");
                        holder.tvCommentPreview.setText(name + ": " + preview);
                    } else {
                        holder.tvCommentPreview.setText("Be the first to comment...");
                    }
                });

        // Like button
        final long[] currentLikes = {likes};
        holder.layoutLike.setOnClickListener(v -> {
            currentLikes[0]++;
            holder.tvLikeCount.setText(String.valueOf(currentLikes[0]));
            holder.imgLike.setImageResource(R.drawable.ic_heart);
            db.collection("posts").document(postId)
                    .update("likes", currentLikes[0]);
        });

        // Comment button
        holder.layoutComment.setOnClickListener(v ->
                commentListener.onCommentClick(postId, holder.tvCommentCount));
        holder.layoutCommentPreview.setOnClickListener(v ->
                commentListener.onCommentClick(postId, holder.tvCommentCount));
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPost, imgHabitIcon, imgLike, imgComment;
        FrameLayout frameHabitIcon, frameVideoPost;
        TextView tvHabitTag, tvDiscoverBadge, tvCaption, tvPostUser,
                tvLikeCount, tvCommentCount, tvCommentPreview, tvViewAll;
        LinearLayout layoutLike, layoutComment, layoutCommentPreview;

        PostViewHolder(View v) {
            super(v);
            imgPost              = v.findViewById(R.id.imgPost);
            frameHabitIcon       = v.findViewById(R.id.frameHabitIcon);
            frameVideoPost       = v.findViewById(R.id.frameVideoPost);
            imgHabitIcon         = v.findViewById(R.id.imgHabitIcon);
            imgLike              = v.findViewById(R.id.imgLike);
            imgComment           = v.findViewById(R.id.imgComment);
            tvHabitTag           = v.findViewById(R.id.tvHabitTag);
            tvDiscoverBadge      = v.findViewById(R.id.tvDiscoverBadge);
            tvCaption            = v.findViewById(R.id.tvCaption);
            tvPostUser           = v.findViewById(R.id.tvPostUser);
            tvLikeCount          = v.findViewById(R.id.tvLikeCount);
            tvCommentCount       = v.findViewById(R.id.tvCommentCount);
            tvCommentPreview     = v.findViewById(R.id.tvCommentPreview);
            tvViewAll            = v.findViewById(R.id.tvViewAll);
            layoutLike           = v.findViewById(R.id.layoutLike);
            layoutComment        = v.findViewById(R.id.layoutComment);
            layoutCommentPreview = v.findViewById(R.id.layoutCommentPreview);
        }
    }
}
