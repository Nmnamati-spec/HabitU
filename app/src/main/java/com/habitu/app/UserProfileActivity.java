package com.habitu.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UserProfileActivity extends AppCompatActivity {

    private String targetUserId;
    private String currentUserId;
    private FirebaseFirestore db;

    private TextView tvProfileName, tvProfileUniversity;
    private TextView tvStreakCount, tvFollowersCount, tvFollowingCount;
    private Button btnFollow;
    private LinearLayout habitsContainer, postsContainer;
    private ImageView btnBack;

    private List<String> currentFollowers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        targetUserId  = getIntent().getStringExtra("userId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db            = FirebaseFirestore.getInstance();

        tvProfileName       = findViewById(R.id.tvProfileName);
        tvProfileUniversity = findViewById(R.id.tvProfileUniversity);
        tvStreakCount       = findViewById(R.id.tvStreakCount);
        tvFollowersCount    = findViewById(R.id.tvFollowersCount);
        tvFollowingCount    = findViewById(R.id.tvFollowingCount);
        btnFollow           = findViewById(R.id.btnFollow);
        btnBack             = findViewById(R.id.btnBack);
        habitsContainer     = findViewById(R.id.habitsContainer);
        postsContainer      = findViewById(R.id.postsContainer);

        btnBack.setOnClickListener(v -> finish());

        if (targetUserId == null) { finish(); return; }

        if (targetUserId.equals(currentUserId)) {
            btnFollow.setVisibility(View.GONE);
        }

        loadProfile();
        loadStreak();
        loadRecentHabits();
        loadRecentPosts();
    }

    private void loadProfile() {
        db.collection("users").document(targetUserId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String firstName  = doc.getString("firstName");
                    String surname    = doc.getString("surname");
                    String university = doc.getString("university");

                    String fullName = ((firstName != null ? firstName : "") + " "
                            + (surname != null ? surname : "")).trim();
                    tvProfileName.setText(fullName);
                    tvProfileUniversity.setText(
                            university != null && !university.isEmpty() ? university : "");

                    List<String> followers = (List<String>) doc.get("followers");
                    List<String> following = (List<String>) doc.get("following");
                    currentFollowers = followers != null ? new ArrayList<>(followers) : new ArrayList<>();

                    tvFollowersCount.setText(String.valueOf(currentFollowers.size()));
                    tvFollowingCount.setText(String.valueOf(following != null ? following.size() : 0));

                    updateFollowButton(currentFollowers.contains(currentUserId));
                    btnFollow.setOnClickListener(v -> toggleFollow());
                });
    }

    private void toggleFollow() {
        boolean isFollowing = currentFollowers.contains(currentUserId);
        if (isFollowing) {
            db.collection("users").document(targetUserId)
                    .update("followers", FieldValue.arrayRemove(currentUserId));
            db.collection("users").document(currentUserId)
                    .update("following", FieldValue.arrayRemove(targetUserId));
            currentFollowers.remove(currentUserId);
        } else {
            db.collection("users").document(targetUserId)
                    .update("followers", FieldValue.arrayUnion(currentUserId));
            db.collection("users").document(currentUserId)
                    .update("following", FieldValue.arrayUnion(targetUserId));
            currentFollowers.add(currentUserId);
        }
        updateFollowButton(!isFollowing);
        tvFollowersCount.setText(String.valueOf(currentFollowers.size()));
    }

    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            btnFollow.setText("Following");
            btnFollow.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#1E2018")));
            btnFollow.setTextColor(Color.parseColor("#D4A832"));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#D4A832")));
            btnFollow.setTextColor(Color.parseColor("#0E0F0C"));
        }
    }

    private void loadStreak() {
        db.collection("users").document(targetUserId)
                .collection("logs").get()
                .addOnSuccessListener(snap ->
                        tvStreakCount.setText(
                                String.valueOf(calculateStreak(snap.getDocuments()))));
    }

    private int calculateStreak(List<DocumentSnapshot> docs) {
        if (docs.isEmpty()) return 0;
        Set<String> loggedDays = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        for (DocumentSnapshot doc : docs) {
            Long ts = doc.getLong("timestamp");
            if (ts != null) loggedDays.add(sdf.format(new Date(ts)));
        }
        int streak = 0;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 365; i++) {
            if (loggedDays.contains(sdf.format(cal.getTime()))) streak++;
            else if (i > 0) break;
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    private void loadRecentHabits() {
        db.collection("users").document(targetUserId)
                .collection("logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snap -> {
                    habitsContainer.removeAllViews();
                    if (snap.isEmpty()) {
                        habitsContainer.addView(emptyText("No habits logged yet"));
                        return;
                    }
                    LinkedHashSet<String> seen = new LinkedHashSet<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String habit = doc.getString("habit");
                        if (habit != null) seen.add(habit);
                    }
                    for (String habit : seen) addHabitChip(habit);
                });
    }

    private void addHabitChip(String habit) {
        float dp = getResources().getDisplayMetrics().density;

        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackground(getDrawable(R.drawable.tag_bg));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, (int)(8 * dp), 0);
        chip.setLayoutParams(p);
        chip.setPadding((int)(10 * dp), (int)(7 * dp), (int)(10 * dp), (int)(7 * dp));

        ImageView icon = new ImageView(this);
        int sz = (int)(18 * dp);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(sz, sz);
        ip.setMargins(0, 0, (int)(6 * dp), 0);
        icon.setLayoutParams(ip);
        icon.setImageResource(HabitIconMapper.getIcon(habit));

        TextView label = new TextView(this);
        label.setText(habit);
        label.setTextSize(12);
        label.setTextColor(Color.parseColor("#D4A832"));
        label.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        chip.addView(icon);
        chip.addView(label);
        habitsContainer.addView(chip);
    }

    private void loadRecentPosts() {
        db.collection("posts")
                .whereEqualTo("userId", targetUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(6)
                .get()
                .addOnSuccessListener(snap -> {
                    postsContainer.removeAllViews();
                    if (snap.isEmpty()) {
                        postsContainer.addView(emptyText("No posts yet"));
                        return;
                    }
                    float dp = getResources().getDisplayMetrics().density;
                    int thumbH = (int)(120 * dp);
                    int gap    = (int)(4 * dp);
                    List<DocumentSnapshot> docs = snap.getDocuments();

                    for (int i = 0; i < docs.size(); i += 2) {
                        LinearLayout row = new LinearLayout(this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        rp.setMargins(0, 0, 0, gap);
                        row.setLayoutParams(rp);

                        row.addView(buildThumb(docs.get(i), thumbH));
                        if (i + 1 < docs.size()) {
                            View spacer = new View(this);
                            spacer.setLayoutParams(new LinearLayout.LayoutParams(gap, thumbH));
                            row.addView(spacer);
                            row.addView(buildThumb(docs.get(i + 1), thumbH));
                        } else {
                            // Placeholder to keep left thumb half-width
                            View ph = new View(this);
                            ph.setLayoutParams(new LinearLayout.LayoutParams(0, thumbH, 1f));
                            row.addView(ph);
                        }
                        postsContainer.addView(row);
                    }
                });
    }

    private View buildThumb(DocumentSnapshot doc, int height) {
        float dp = getResources().getDisplayMetrics().density;
        ImageView img = new ImageView(this);
        img.setLayoutParams(new LinearLayout.LayoutParams(0, height, 1f));
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setBackgroundColor(Color.parseColor("#222420"));

        String imageUrl = doc.getString("imageUrl");
        Boolean isVideo = doc.getBoolean("isVideo");
        String habit    = doc.getString("habit");

        if (imageUrl != null && !imageUrl.isEmpty() && !Boolean.TRUE.equals(isVideo)) {
            Glide.with(this).load(imageUrl).into(img);
        } else {
            img.setImageResource(HabitIconMapper.getIcon(habit));
            int pad = (int)(24 * dp);
            img.setPadding(pad, pad, pad, pad);
        }
        return img;
    }

    private TextView emptyText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#6B6D65"));
        tv.setTextSize(13);
        return tv;
    }
}
