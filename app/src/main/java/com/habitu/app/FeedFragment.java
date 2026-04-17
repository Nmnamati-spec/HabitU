package com.habitu.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class FeedFragment extends Fragment {

    RecyclerView rvFeed;
    PostAdapter adapter;
    List<DocumentSnapshot> postList = new ArrayList<>();
    FirebaseFirestore db;
    String userId;
    List<String> userHabits = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvFeed = view.findViewById(R.id.rvFeed);

        // Pinterest 2 column staggered grid
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2,
                        StaggeredGridLayoutManager.VERTICAL);
        rvFeed.setLayoutManager(layoutManager);

        adapter = new PostAdapter(postList, getContext(), userId, db,
                (postId, btnComment) -> openCommentSheet(postId, btnComment));
        rvFeed.setAdapter(adapter);

        // Upload button
        view.findViewById(R.id.btnUploadPost).setOnClickListener(v ->
                startActivity(new Intent(getActivity(),
                        UploadPostActivity.class)));

        loadUserHabitsThenFeed();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserHabitsThenFeed();
    }

    private void loadUserHabitsThenFeed() {
        db.collection("habits").whereEqualTo("userId", userId)
                .get().addOnSuccessListener(snap -> {
                    userHabits.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) userHabits.add(name.toLowerCase());
                    }
                    loadFeed();
                });
    }

    private void loadFeed() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(40)
                .get()
                .addOnSuccessListener(snap -> {
                    List<DocumentSnapshot> myHabitPosts  = new ArrayList<>();
                    List<DocumentSnapshot> discoverPosts = new ArrayList<>();

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String habit = doc.getString("habit");
                        if (habit == null) continue;

                        boolean isMyHabit = false;
                        for (String h : userHabits) {
                            if (habit.toLowerCase().contains(h)) {
                                isMyHabit = true;
                                break;
                            }
                        }
                        if (isMyHabit) myHabitPosts.add(doc);
                        else           discoverPosts.add(doc);
                    }

                    // Mark discover posts
                    List<DocumentSnapshot> discoverSlice =
                            discoverPosts.subList(0,
                                    Math.min(6, discoverPosts.size()));

                    postList.clear();
                    postList.addAll(myHabitPosts);
                    postList.addAll(discoverSlice);
                    adapter.notifyDataSetChanged();
                });
    }

    private void openCommentSheet(String postId, TextView btnComment) {
        BottomSheetDialog sheet = new BottomSheetDialog(
                requireContext(), R.style.BottomSheetTheme);
        View sheetView = LayoutInflater.from(getContext())
                .inflate(R.layout.sheet_comments, null);
        sheet.setContentView(sheetView);

        androidx.recyclerview.widget.RecyclerView rv =
                sheetView.findViewById(R.id.rvComments);
        android.widget.EditText etComment =
                sheetView.findViewById(R.id.etComment);
        TextView btnSend  = sheetView.findViewById(R.id.btnSendComment);
        TextView btnClose = sheetView.findViewById(R.id.btnCloseSheet);

        List<Map<String, Object>> commentList = new ArrayList<>();
        CommentAdapter commentAdapter = new CommentAdapter(commentList);
        rv.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(
                        getContext()));
        rv.setAdapter(commentAdapter);

        // Load comments in real time
        db.collection("posts").document(postId)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    commentList.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Map<String, Object> c = new HashMap<>();
                        c.put("userName", doc.getString("userName"));
                        c.put("text",     doc.getString("text"));
                        commentList.add(c);
                    }
                    commentAdapter.notifyDataSetChanged();
                    btnComment.setText("💬 " + commentList.size());
                    if (!commentList.isEmpty())
                        rv.scrollToPosition(commentList.size() - 1);
                });

        // Send comment
        btnSend.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (text.isEmpty()) return;

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("firstName")
                                + " " + doc.getString("surname");
                        Map<String, Object> comment = new HashMap<>();
                        comment.put("userId",    userId);
                        comment.put("userName",  name);
                        comment.put("text",      text);
                        comment.put("timestamp", System.currentTimeMillis());
                        db.collection("posts").document(postId)
                                .collection("comments").add(comment);
                        etComment.setText("");
                    });
        });

        btnClose.setOnClickListener(v -> sheet.dismiss());
        sheet.show();
    }
}