package com.habitu.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class CommunitiesFragment extends Fragment {

    LinearLayout communitiesContainer;
    FirebaseFirestore db;
    String userId;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_communities, container, false);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        communitiesContainer = view.findViewById(R.id.communitiesContainer);

        FloatingActionButton fab = view.findViewById(R.id.fabCreateCommunity);
        fab.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CreateCommunityActivity.class)));

        loadCommunities();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCommunities();
    }

    private void loadCommunities() {
        db.collection("communities")
                .whereEqualTo("isVisible", true)
                .get()
                .addOnSuccessListener(snap -> {
                    communitiesContainer.removeAllViews();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        addCommunityCard(doc);
                    }
                });
    }

    private void addCommunityCard(DocumentSnapshot doc) {
        String commId  = doc.getId();
        String name    = doc.getString("name");
        String adminId = doc.getString("adminId");
        boolean locked = Boolean.TRUE.equals(doc.getBoolean("isLocked"));
        List<String> members = (List<String>) doc.get("memberIds");
        List<String> pending = (List<String>) doc.get("pendingRequestIds");

        boolean isMember  = members != null && members.contains(userId);
        boolean isPending = pending != null && pending.contains(userId);
        boolean isAdmin   = userId.equals(adminId);
        int memberCount   = members != null ? members.size() : 0;

        // Card
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.input_bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 14);
        card.setLayoutParams(params);
        card.setPadding(36, 32, 36, 32);
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Community icon
        android.widget.ImageView imgIcon = new android.widget.ImageView(getContext());
        imgIcon.setImageResource(R.drawable.ic_group);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(52, 52);
        iconParams.setMarginEnd(24);
        imgIcon.setLayoutParams(iconParams);
        imgIcon.setPadding(10, 10, 10, 10);
        imgIcon.setBackgroundResource(R.drawable.icon_container_bg);

        // Text group
        LinearLayout textGroup = new LinearLayout(getContext());
        textGroup.setOrientation(LinearLayout.VERTICAL);
        textGroup.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvName = new TextView(getContext());
        tvName.setText(name + (isAdmin ? " (Admin)" : ""));
        tvName.setTextSize(14);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setTextColor(0xFFF4F5F0);

        TextView tvMeta = new TextView(getContext());
        tvMeta.setText(memberCount + " members · " + (locked ? "Locked" : "Open"));
        tvMeta.setTextSize(11);
        tvMeta.setTextColor(0xFF6B6D65);

        textGroup.addView(tvName);
        textGroup.addView(tvMeta);

        // Action buttons container
        LinearLayout actionGroup = new LinearLayout(getContext());
        actionGroup.setOrientation(LinearLayout.VERTICAL);
        actionGroup.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        if (isAdmin || isMember) {
            // Open button → community chat
            TextView btnOpen = new TextView(getContext());
            btnOpen.setText("Open");
            btnOpen.setTextSize(11);
            btnOpen.setTypeface(null, android.graphics.Typeface.BOLD);
            btnOpen.setPadding(24, 16, 24, 16);
            btnOpen.setTextColor(0xFFC8F53B);
            btnOpen.setOnClickListener(v -> openChat(commId, name, adminId, memberCount));
            actionGroup.addView(btnOpen);

            if (isAdmin) {
                TextView btnSettings = new TextView(getContext());
                btnSettings.setText("Settings");
                btnSettings.setTextSize(10);
                btnSettings.setPadding(24, 6, 24, 6);
                btnSettings.setTextColor(0xFF6B6D65);
                btnSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), CommunitySettingsActivity.class);
                    intent.putExtra("communityId",   commId);
                    intent.putExtra("communityName", name);
                    startActivity(intent);
                });
                actionGroup.addView(btnSettings);
            }
        } else if (isPending) {
            TextView btn = new TextView(getContext());
            btn.setText("Requested");
            btn.setTextSize(11);
            btn.setTypeface(null, android.graphics.Typeface.BOLD);
            btn.setPadding(24, 16, 24, 16);
            btn.setTextColor(0xFFFF6B35);
            actionGroup.addView(btn);
        } else if (locked) {
            TextView btn = new TextView(getContext());
            btn.setText("Request");
            btn.setTextSize(11);
            btn.setTypeface(null, android.graphics.Typeface.BOLD);
            btn.setPadding(24, 16, 24, 16);
            btn.setTextColor(0xFFFF6B35);
            btn.setOnClickListener(v ->
                    db.collection("communities").document(commId)
                            .update("pendingRequestIds", FieldValue.arrayUnion(userId))
                            .addOnSuccessListener(x -> {
                                btn.setText("Requested");
                                Toast.makeText(getContext(), "Request sent!", Toast.LENGTH_SHORT).show();
                            }));
            actionGroup.addView(btn);
        } else {
            TextView btn = new TextView(getContext());
            btn.setText("Join");
            btn.setTextSize(11);
            btn.setTypeface(null, android.graphics.Typeface.BOLD);
            btn.setPadding(24, 16, 24, 16);
            btn.setTextColor(0xFFC8F53B);
            btn.setOnClickListener(v ->
                    db.collection("communities").document(commId)
                            .update("memberIds", FieldValue.arrayUnion(userId))
                            .addOnSuccessListener(x -> {
                                Toast.makeText(getContext(), "Joined " + name + "!", Toast.LENGTH_SHORT).show();
                                loadCommunities();
                            }));
            actionGroup.addView(btn);
        }

        card.addView(imgIcon);
        card.addView(textGroup);
        card.addView(actionGroup);
        communitiesContainer.addView(card);
    }

    private void openChat(String commId, String name, String adminId, int memberCount) {
        Intent intent = new Intent(getActivity(), CommunityChatActivity.class);
        intent.putExtra("communityId",   commId);
        intent.putExtra("communityName", name);
        intent.putExtra("adminId",       adminId);
        intent.putExtra("memberCount",   memberCount);
        startActivity(intent);
    }
}
