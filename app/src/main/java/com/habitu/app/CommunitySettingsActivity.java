package com.habitu.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.firebase.firestore.FirebaseFirestore;

public class CommunitySettingsActivity extends AppCompatActivity {

    TextView tvCommIcon, tvCommName, tvCommMeta,
            tvPendingCount, btnBack;
    SwitchCompat switchLocked, switchVisible;
    LinearLayout rowMemberRequests, btnDeleteCommunity;

    FirebaseFirestore db;
    String communityId, communityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_settings);

        db            = FirebaseFirestore.getInstance();
        communityId   = getIntent().getStringExtra("communityId");
        communityName = getIntent().getStringExtra("communityName");

        tvCommIcon         = findViewById(R.id.tvCommIcon);
        tvCommName         = findViewById(R.id.tvCommName);
        tvCommMeta         = findViewById(R.id.tvCommMeta);
        tvPendingCount     = findViewById(R.id.tvPendingCount);
        switchLocked       = findViewById(R.id.switchLocked);
        switchVisible      = findViewById(R.id.switchVisible);
        rowMemberRequests  = findViewById(R.id.rowMemberRequests);
        btnDeleteCommunity = findViewById(R.id.btnDeleteCommunity);
        btnBack            = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadCommunityData();

        // Locked toggle
        switchLocked.setOnCheckedChangeListener((btn, isChecked) ->
                db.collection("communities").document(communityId)
                        .update("isLocked", isChecked)
                        .addOnSuccessListener(v ->
                                Toast.makeText(this,
                                        "Community is now " + (isChecked ? "Locked 🔒" : "Open 🌱"),
                                        Toast.LENGTH_SHORT).show()));

        // Visible toggle
        switchVisible.setOnCheckedChangeListener((btn, isChecked) ->
                db.collection("communities").document(communityId)
                        .update("isVisible", isChecked));

        // Member requests
        rowMemberRequests.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemberRequestsActivity.class);
            intent.putExtra("communityId",   communityId);
            intent.putExtra("communityName", communityName);
            startActivity(intent);
        });

        // Delete community
        btnDeleteCommunity.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete Community?")
                        .setMessage("This will permanently delete " + communityName + " and cannot be undone.")
                        .setPositiveButton("Delete", (d, w) ->
                                db.collection("communities").document(communityId)
                                        .delete()
                                        .addOnSuccessListener(x -> {
                                            Toast.makeText(this,
                                                    "Community deleted", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }))
                        .setNegativeButton("Cancel", null)
                        .show());
    }

    private void loadCommunityData() {
        db.collection("communities").document(communityId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String icon    = doc.getString("icon");
                    String name    = doc.getString("name");
                    boolean locked  = Boolean.TRUE.equals(doc.getBoolean("isLocked"));
                    boolean visible = Boolean.TRUE.equals(doc.getBoolean("isVisible"));

                    java.util.List<?> pending = (java.util.List<?>)
                            doc.get("pendingRequestIds");
                    java.util.List<?> members = (java.util.List<?>)
                            doc.get("memberIds");

                    int pendingCount = pending != null ? pending.size() : 0;
                    int memberCount  = members != null ? members.size() : 0;

                    tvCommIcon.setText(icon != null ? icon : "👥");
                    tvCommName.setText(name);
                    tvCommMeta.setText(memberCount + " members • You are the Admin");

                    switchLocked.setChecked(locked);
                    switchVisible.setChecked(visible);

                    if (pendingCount > 0) {
                        tvPendingCount.setVisibility(android.view.View.VISIBLE);
                        tvPendingCount.setText(String.valueOf(pendingCount));
                    }
                });
    }
}
