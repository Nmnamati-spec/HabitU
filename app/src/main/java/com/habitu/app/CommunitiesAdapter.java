package com.habitu.app;

import android.animation.AnimatorInflater;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class CommunitiesAdapter extends RecyclerView.Adapter<CommunitiesAdapter.VH> {

    // Contrasting accent colors — all pop on the dark/lime app theme
    // Indigo, Amber, Rose, Cyan
    private static final int[] ACCENT   = {0xFF818CF8, 0xFFFBBF24, 0xFFFB7185, 0xFF22D3EE};
    // Darker shade of each accent for the top of the gradient
    private static final int[] DARK_BG  = {0xFF1A1640, 0xFF3D2800, 0xFF4A0F1E, 0xFF003D4A};
    private static final int[] ICONS    = {R.drawable.ic_study, R.drawable.ic_run, R.drawable.ic_gym, R.drawable.ic_meditation};
    private static final String[] TAGS  = {"STUDY", "RUNNING", "FITNESS", "WELLNESS"};

    // App action colors — consistent with the rest of the app
    private static final int LIME        = 0xFFD4A832;
    private static final int LIME_BG     = 0xFF261E0A;
    private static final int ORANGE      = 0xFFFF6B35;
    private static final int ORANGE_BG   = 0xFF2A1400;

    private final List<DocumentSnapshot> docs = new ArrayList<>();
    private final FirebaseFirestore db;
    private final String userId;
    private final FragmentActivity activity;
    private Runnable onReload;

    CommunitiesAdapter(FirebaseFirestore db, String userId, FragmentActivity activity) {
        this.db = db;
        this.userId = userId;
        this.activity = activity;
    }

    void setOnReload(Runnable r) { this.onReload = r; }

    void setData(List<DocumentSnapshot> data) {
        docs.clear();
        docs.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DocumentSnapshot doc = docs.get(position);
        String commId  = doc.getId();
        String name    = doc.getString("name");
        String adminId = doc.getString("adminId");
        boolean locked = Boolean.TRUE.equals(doc.getBoolean("isLocked"));
        @SuppressWarnings("unchecked")
        List<String> members = (List<String>) doc.get("memberIds");
        @SuppressWarnings("unchecked")
        List<String> pending = (List<String>) doc.get("pendingRequestIds");

        boolean isMember  = members != null && members.contains(userId);
        boolean isPending = pending != null && pending.contains(userId);
        boolean isAdmin   = userId.equals(adminId);
        int memberCount   = members != null ? members.size() : 0;

        int idx    = Math.abs(name != null ? name.hashCode() : 0) % 4;
        int accent = ACCENT[idx];
        int darkBg = DARK_BG[idx];

        // — Gradient thumbnail —
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{darkBg, 0xFF0E0F0C}
        );
        h.thumbGradient.setBackground(gradient);

        // Icon: white tint so it reads on any accent bg
        h.imgThumb.setImageResource(ICONS[idx]);
        h.imgThumb.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_ATOP);

        // Category chip: accent-colored text on semi-dark pill
        h.tvCategory.setText(TAGS[idx]);
        h.tvCategory.setTextColor(accent);
        GradientDrawable chipBg = new GradientDrawable();
        chipBg.setShape(GradientDrawable.RECTANGLE);
        chipBg.setCornerRadius(dp(20));
        chipBg.setColor(0xCC0E0F0C); // 80% opacity black
        h.tvCategory.setBackground(chipBg);

        // Card stroke: accent at 25% opacity — subtle glow
        MaterialCardView card = (MaterialCardView) h.itemView;
        card.setStrokeColor(Color.argb(64,
                Color.red(accent), Color.green(accent), Color.blue(accent)));

        // Name + member count
        h.tvCommunityName.setText(name);
        h.tvMemberCount.setText(memberCount + " members");

        // — Action button + card click —
        if (isAdmin) {
            setPill(h.btnAction, "Settings", LIME_BG, LIME);
            h.btnAction.setOnClickListener(v -> {
                Intent i = new Intent(activity, CommunitySettingsActivity.class);
                i.putExtra("communityId",   commId);
                i.putExtra("communityName", name);
                activity.startActivity(i);
            });
            card.setOnClickListener(v -> openChat(commId, name, adminId, memberCount));
        } else if (isMember) {
            setPill(h.btnAction, "Open", LIME_BG, LIME);
            h.btnAction.setOnClickListener(v -> openChat(commId, name, adminId, memberCount));
            card.setOnClickListener(v -> openChat(commId, name, adminId, memberCount));
        } else if (isPending) {
            setPill(h.btnAction, "Pending", ORANGE_BG, ORANGE);
            h.btnAction.setOnClickListener(null);
            card.setOnClickListener(null);
        } else if (locked) {
            setPill(h.btnAction, "Request", ORANGE_BG, ORANGE);
            h.btnAction.setOnClickListener(v ->
                db.collection("communities").document(commId)
                    .update("pendingRequestIds", FieldValue.arrayUnion(userId))
                    .addOnSuccessListener(x -> {
                        Toast.makeText(activity, "Request sent!", Toast.LENGTH_SHORT).show();
                        setPill(h.btnAction, "Pending", ORANGE_BG, ORANGE);
                        h.btnAction.setOnClickListener(null);
                    }));
            card.setOnClickListener(null);
        } else {
            setPill(h.btnAction, "Join", LIME_BG, LIME);
            h.btnAction.setOnClickListener(v ->
                db.collection("communities").document(commId)
                    .update("memberIds", FieldValue.arrayUnion(userId))
                    .addOnSuccessListener(x -> {
                        Toast.makeText(activity, "Joined " + name + "!", Toast.LENGTH_SHORT).show();
                        if (onReload != null) onReload.run();
                    }));
            card.setOnClickListener(null);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VH h) {
        super.onViewAttachedToWindow(h);
        int pos = h.getBindingAdapterPosition();
        long delay = Math.min(pos * 55L, 280L);
        h.itemView.setAlpha(0f);
        h.itemView.setTranslationY(dp(28));
        h.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(320)
                .setInterpolator(new DecelerateInterpolator(2.0f))
                .setStartDelay(delay)
                .start();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VH h) {
        super.onViewDetachedFromWindow(h);
        h.itemView.animate().cancel();
        h.itemView.setAlpha(1f);
        h.itemView.setTranslationY(0f);
    }

    private void setPill(TextView tv, String text, int bgColor, int textColor) {
        tv.setText(text);
        tv.setTextColor(textColor);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(20));
        bg.setColor(bgColor);
        tv.setBackground(bg);
    }

    private float dp(int value) {
        return value * activity.getResources().getDisplayMetrics().density;
    }

    private void openChat(String commId, String name, String adminId, int memberCount) {
        Intent intent = new Intent(activity, CommunityChatActivity.class);
        intent.putExtra("communityId",   commId);
        intent.putExtra("communityName", name);
        intent.putExtra("adminId",       adminId);
        intent.putExtra("memberCount",   memberCount);
        activity.startActivity(intent);
    }

    @Override
    public int getItemCount() { return docs.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View        thumbGradient;
        ImageView   imgThumb;
        TextView    tvCategory, tvCommunityName, tvMemberCount, btnAction;

        VH(@NonNull View v) {
            super(v);
            thumbGradient   = v.findViewById(R.id.thumbGradient);
            imgThumb        = v.findViewById(R.id.imgThumb);
            tvCategory      = v.findViewById(R.id.tvCategory);
            tvCommunityName = v.findViewById(R.id.tvCommunityName);
            tvMemberCount   = v.findViewById(R.id.tvMemberCount);
            btnAction       = v.findViewById(R.id.btnAction);
        }
    }
}
