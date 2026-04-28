package com.habitu.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private final List<DocumentSnapshot> users;
    private final Context context;
    private final String currentUserId;
    private final FirebaseFirestore db;
    // Tracks which users the current user is following (local state for this session)
    private final Set<String> followingIds = new HashSet<>();

    public UserSearchAdapter(List<DocumentSnapshot> users, Context context,
                             String currentUserId, FirebaseFirestore db) {
        this.users = users;
        this.context = context;
        this.currentUserId = currentUserId;
        this.db = db;

        // Load current user's following list once
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    List<String> following = (List<String>) doc.get("following");
                    if (following != null) followingIds.addAll(following);
                    notifyDataSetChanged();
                });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        DocumentSnapshot doc = users.get(position);

        String targetId  = doc.getString("userId");
        String firstName = doc.getString("firstName");
        String surname   = doc.getString("surname");
        String university = doc.getString("university");

        String fullName = ((firstName != null ? firstName : "") + " "
                + (surname != null ? surname : "")).trim();
        holder.tvUserName.setText(fullName);
        holder.tvUserUniversity.setText(university != null ? university : "");

        boolean isFollowing = targetId != null && followingIds.contains(targetId);
        setFollowButtonState(holder.btnFollow, isFollowing);

        holder.btnFollow.setOnClickListener(v -> {
            if (targetId == null || targetId.equals(currentUserId)) return;
            boolean currently = followingIds.contains(targetId);
            if (currently) {
                db.collection("users").document(targetId)
                        .update("followers", FieldValue.arrayRemove(currentUserId));
                db.collection("users").document(currentUserId)
                        .update("following", FieldValue.arrayRemove(targetId));
                followingIds.remove(targetId);
            } else {
                db.collection("users").document(targetId)
                        .update("followers", FieldValue.arrayUnion(currentUserId));
                db.collection("users").document(currentUserId)
                        .update("following", FieldValue.arrayUnion(targetId));
                followingIds.add(targetId);
            }
            setFollowButtonState(holder.btnFollow, !currently);
        });

        holder.itemView.setOnClickListener(v -> {
            if (targetId == null) return;
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", targetId);
            context.startActivity(intent);
        });
    }

    private void setFollowButtonState(Button btn, boolean isFollowing) {
        if (isFollowing) {
            btn.setText("Following");
            btn.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#1E2018")));
            btn.setTextColor(Color.parseColor("#D4A832"));
        } else {
            btn.setText("Follow");
            btn.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#D4A832")));
            btn.setTextColor(Color.parseColor("#0E0F0C"));
        }
    }

    @Override
    public int getItemCount() { return users.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserUniversity;
        Button btnFollow;

        UserViewHolder(View v) {
            super(v);
            tvUserName       = v.findViewById(R.id.tvUserName);
            tvUserUniversity = v.findViewById(R.id.tvUserUniversity);
            btnFollow        = v.findViewById(R.id.btnFollow);
        }
    }
}
