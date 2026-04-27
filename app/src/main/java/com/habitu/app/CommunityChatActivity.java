package com.habitu.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class CommunityChatActivity extends AppCompatActivity {

    RecyclerView rvMessages;
    EditText etMessage;
    ImageButton btnSend, btnBack, btnSettings;
    TextView tvCommunityName, tvMemberCount;

    FirebaseFirestore db;
    String userId, userName;
    String communityId, communityName, adminId;
    int memberCount;

    final List<ChatMessage> messages = new ArrayList<>();
    ChatMessageAdapter adapter;
    ListenerRegistration messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat);

        communityId   = getIntent().getStringExtra("communityId");
        communityName = getIntent().getStringExtra("communityName");
        adminId       = getIntent().getStringExtra("adminId");
        memberCount   = getIntent().getIntExtra("memberCount", 0);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvMessages      = findViewById(R.id.rvMessages);
        etMessage       = findViewById(R.id.etMessage);
        btnSend         = findViewById(R.id.btnSend);
        btnBack         = findViewById(R.id.btnBack);
        btnSettings     = findViewById(R.id.btnSettings);
        tvCommunityName = findViewById(R.id.tvCommunityName);
        tvMemberCount   = findViewById(R.id.tvMemberCount);

        tvCommunityName.setText(communityName);
        tvMemberCount.setText(memberCount + " members");

        if (userId.equals(adminId)) {
            btnSettings.setVisibility(View.VISIBLE);
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, CommunitySettingsActivity.class);
                intent.putExtra("communityId",   communityId);
                intent.putExtra("communityName", communityName);
                startActivity(intent);
            });
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        adapter = new ChatMessageAdapter(messages, userId);
        rvMessages.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());

        loadUserName();
        listenForMessages();
    }

    private void loadUserName() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String first   = doc.getString("firstName");
                        String surname = doc.getString("surname");
                        userName = ((first != null ? first : "") + " " + (surname != null ? surname : "")).trim();
                    }
                    if (userName == null || userName.isEmpty()) userName = "Unknown";
                });
    }

    private void listenForMessages() {
        messageListener = db.collection("communities")
                .document(communityId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    messages.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) messages.add(msg);
                    }
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        rvMessages.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        etMessage.setText("");

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId",   userId);
        msg.put("senderName", userName != null ? userName : "Unknown");
        msg.put("text",       text);
        msg.put("timestamp",  Timestamp.now());

        db.collection("communities")
                .document(communityId)
                .collection("messages")
                .add(msg)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) messageListener.remove();
    }
}
