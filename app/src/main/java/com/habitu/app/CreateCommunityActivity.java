package com.habitu.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class CreateCommunityActivity extends AppCompatActivity {

    EditText etCommunityName;
    SwitchCompat switchLocked, switchVisible;
    Button btnCreate;
    ImageButton btnBack;

    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        etCommunityName = findViewById(R.id.etCommunityName);
        switchLocked    = findViewById(R.id.switchLocked);
        switchVisible   = findViewById(R.id.switchVisible);
        btnCreate       = findViewById(R.id.btnCreate);
        btnBack         = findViewById(R.id.btnBack);

        switchVisible.setChecked(true);

        btnBack.setOnClickListener(v -> finish());
        btnCreate.setOnClickListener(v -> createCommunity());
    }

    private void createCommunity() {
        String name = etCommunityName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etCommunityName.setError("Community name is required");
            return;
        }

        btnCreate.setEnabled(false);
        btnCreate.setText("Creating...");

        Map<String, Object> community = new HashMap<>();
        community.put("name",              name);
        community.put("adminId",           userId);
        community.put("isLocked",          switchLocked.isChecked());
        community.put("isVisible",         switchVisible.isChecked());
        community.put("memberIds",         Collections.singletonList(userId));
        community.put("pendingRequestIds", new ArrayList<>());
        community.put("icon",              "ic_group");

        db.collection("communities")
                .add(community)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, name + " created!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnCreate.setEnabled(true);
                    btnCreate.setText("Create Community");
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
