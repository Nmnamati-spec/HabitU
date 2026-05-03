package com.habitu.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    List<DocumentSnapshot> posts;
    Context context;
    String userId;
    FirebaseFirestore db;
    OnCommentClickListener commentListener;

    private final Set<Integer> animatedPositions = new HashSet<>();
    private final PathInterpolator easeOut = new PathInterpolator(0.23f, 1f, 0.32f, 1f);

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

    public void resetAnimations() {
        animatedPositions.clear();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        DocumentSnapshot doc = posts.get(position);
        String imageUrl = doc.getString("imageUrl");

        String postId     = doc.getId();
        String postUserId = doc.getString("userId");
        String userName   = doc.getString("userName");
        String caption    = doc.getString("caption");
        String habit      = doc.getString("habit");
        boolean isVideo  = doc.getBoolean("isVideo") != null && doc.getBoolean("isVideo");
        boolean discover = doc.getBoolean("discover") != null && doc.getBoolean("discover");
        long likes = doc.getLong("likes") != null ? doc.getLong("likes") : 0;

        holder.tvHabitTag.setText(habit);
        holder.tvCaption.setText(caption);
        holder.tvPostUser.setText(userName);

        // Tap username → open that user's profile
        if (postUserId != null && !postUserId.equals(userId)) {
            holder.tvPostUser.setPaintFlags(
                    holder.tvPostUser.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.tvPostUser.setOnClickListener(v -> {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("userId", postUserId);
                context.startActivity(intent);
            });
        } else {
            holder.tvPostUser.setPaintFlags(
                    holder.tvPostUser.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            holder.tvPostUser.setOnClickListener(null);
        }
        holder.tvLikeCount.setText(String.valueOf(likes));

        holder.tvDiscoverBadge.setVisibility(discover ? View.VISIBLE : View.GONE);

        if (isVideo && imageUrl != null && !imageUrl.isEmpty()) {
            holder.frameVideoPost.setVisibility(View.VISIBLE);
            holder.frameImageContainer.setVisibility(View.GONE);
            holder.frameHabitIcon.setVisibility(View.GONE);
            holder.tvHabitTagContent.setVisibility(View.GONE);
            final String videoUrl = imageUrl;
            holder.frameVideoPost.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(videoUrl), "video/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            });
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.frameImageContainer.setVisibility(View.VISIBLE);
            holder.imgPost.setVisibility(View.VISIBLE);
            holder.frameHabitIcon.setVisibility(View.GONE);
            holder.frameVideoPost.setVisibility(View.GONE);
            holder.tvHabitTagContent.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.imgPost);
        } else {
            holder.frameImageContainer.setVisibility(View.GONE);
            holder.imgPost.setVisibility(View.GONE);
            holder.frameHabitIcon.setVisibility(View.VISIBLE);
            holder.frameVideoPost.setVisibility(View.GONE);
            holder.tvHabitTagContent.setText(habit);
            holder.tvHabitTagContent.setVisibility(View.VISIBLE);
            holder.imgHabitIcon.setImageResource(HabitIconMapper.getIcon(habit));
        }

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

        final long[] currentLikes = {likes};
        holder.layoutLike.setOnClickListener(v -> {
            currentLikes[0]++;
            holder.tvLikeCount.setText(String.valueOf(currentLikes[0]));
            holder.imgLike.setImageResource(R.drawable.ic_heart);
            holder.imgLike.setColorFilter(
                    context.getResources().getColor(R.color.golden_hour, context.getTheme()));
            animateLike(holder.imgLike);
            db.collection("posts").document(postId)
                    .update("likes", currentLikes[0]);
        });

        holder.layoutComment.setOnClickListener(v ->
                commentListener.onCommentClick(postId, holder.tvCommentCount));
        holder.layoutCommentPreview.setOnClickListener(v ->
                commentListener.onCommentClick(postId, holder.tvCommentCount));

        // Stagger entrance animation — only plays once per position per data load
        if (!animatedPositions.contains(position)) {
            animatedPositions.add(position);
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationY(40f);
            long delay = Math.min(position * 55L, 400L);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(360)
                    .setStartDelay(delay)
                    .setInterpolator(easeOut)
                    .start();
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setTranslationY(0f);
        }
    }

    private void animateLike(ImageView imgLike) {
        ObjectAnimator scaleUpX   = ObjectAnimator.ofFloat(imgLike, "scaleX", 1f, 1.4f);
        ObjectAnimator scaleUpY   = ObjectAnimator.ofFloat(imgLike, "scaleY", 1f, 1.4f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(imgLike, "scaleX", 1.4f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(imgLike, "scaleY", 1.4f, 1f);

        scaleUpX.setDuration(130);
        scaleUpY.setDuration(130);
        scaleDownX.setDuration(110);
        scaleDownY.setDuration(110);

        PathInterpolator bounceOut = new PathInterpolator(0.23f, 1f, 0.32f, 1f);
        scaleUpX.setInterpolator(bounceOut);
        scaleUpY.setInterpolator(bounceOut);
        scaleDownX.setInterpolator(bounceOut);
        scaleDownY.setInterpolator(bounceOut);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);

        AnimatorSet full = new AnimatorSet();
        full.playSequentially(scaleUp, scaleDown);
        full.start();
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPost, imgHabitIcon, imgLike, imgComment;
        FrameLayout frameHabitIcon, frameVideoPost, frameImageContainer;
        TextView tvHabitTag, tvHabitTagContent, tvDiscoverBadge, tvCaption, tvPostUser,
                tvLikeCount, tvCommentCount, tvCommentPreview, tvViewAll;
        LinearLayout layoutLike, layoutComment, layoutCommentPreview;

        PostViewHolder(View v) {
            super(v);
            imgPost              = v.findViewById(R.id.imgPost);
            frameHabitIcon       = v.findViewById(R.id.frameHabitIcon);
            frameVideoPost       = v.findViewById(R.id.frameVideoPost);
            frameImageContainer  = v.findViewById(R.id.frameImageContainer);
            imgHabitIcon         = v.findViewById(R.id.imgHabitIcon);
            imgLike              = v.findViewById(R.id.imgLike);
            imgComment           = v.findViewById(R.id.imgComment);
            tvHabitTag           = v.findViewById(R.id.tvHabitTag);
            tvHabitTagContent    = v.findViewById(R.id.tvHabitTagContent);
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
