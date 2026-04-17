package com.habitu.app;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.*;

public class MemberRequestsActivity extends AppCompatActivity {

    RecyclerView rvRequests;
    TextView tvBannerCount, tvBannerCommunity, btnBack;
    FirebaseFirestore db;
    String communityId, communityName;
    List<Map<String, Object>> requestList = new ArrayList<>();
    RequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_requests);

        db            = FirebaseFirestore.getInstance();
        communityId   = getIntent().getStringExtra("communityId");
        communityName = getIntent().getStringExtra("communityName");

        rvRequests        = findViewById(R.id.rvRequests);
        tvBannerCount     = findViewById(R.id.tvBannerCount);
        tvBannerCommunity = findViewById(R.id.tvBannerCommunity);
        btnBack           = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        tvBannerCommunity.setText(communityName + " • Locked community");

        adapter = new RequestAdapter(requestList, communityId, db,
                () -> loadRequests());
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        db.collection("communities").document(communityId).get()
                .addOnSuccessListener(doc -> {
                    List<String> pendingIds = (List<String>)
                            doc.get("pendingRequestIds");

                    if (pendingIds == null || pendingIds.isEmpty()) {
                        tvBannerCount.setText("No pending requests");
                        requestList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    tvBannerCount.setText(pendingIds.size()
                            + " student" + (pendingIds.size() == 1 ? "" : "s")
                            + " waiting to join");

                    requestList.clear();

                    // Load each user's data
                    for (String userId : pendingIds) {
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    Map<String, Object> request = new HashMap<>();
                                    request.put("userId",
                                            userId);
                                    request.put("name",
                                            userDoc.getString("firstName") + " "
                                                    + userDoc.getString("surname"));
                                    request.put("university",
                                            userDoc.getString("university") != null
                                                    ? userDoc.getString("university")
                                                    : "University not set");
                                    requestList.add(request);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }
}
