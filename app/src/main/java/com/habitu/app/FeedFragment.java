package com.habitu.app;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class FeedFragment extends Fragment {

    // Feed
    RecyclerView rvFeed;
    PostAdapter adapter;
    List<DocumentSnapshot> postList = new ArrayList<>();

    // Search
    RecyclerView rvSearchResults;
    UserSearchAdapter searchAdapter;
    List<DocumentSnapshot> searchResults = new ArrayList<>();
    View layoutSearch;
    ImageView btnSearch;
    EditText etSearch;
    TextView btnCancelSearch;

    FirebaseFirestore db;
    String userId;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Feed RecyclerView
        rvFeed = view.findViewById(R.id.rvFeed);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvFeed.setLayoutManager(layoutManager);
        adapter = new PostAdapter(postList, getContext(), userId, db,
                (postId, tvCommentCount) -> openCommentSheet(postId, tvCommentCount));
        rvFeed.setAdapter(adapter);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(300);
        itemAnimator.setChangeDuration(200);
        itemAnimator.setMoveDuration(280);
        itemAnimator.setRemoveDuration(200);
        rvFeed.setItemAnimator(itemAnimator);

        // Search views
        layoutSearch    = view.findViewById(R.id.layoutSearch);
        btnSearch       = view.findViewById(R.id.btnSearch);
        etSearch        = view.findViewById(R.id.etSearch);
        btnCancelSearch = view.findViewById(R.id.btnCancelSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);

        searchAdapter = new UserSearchAdapter(searchResults, getContext(), userId, db);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(searchAdapter);

        btnSearch.setOnClickListener(v -> openSearch());
        btnCancelSearch.setOnClickListener(v -> closeSearch());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim();
                if (q.isEmpty()) showFeed();
                else searchUsers(q);
            }
        });

        loadFeed();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFeed();
    }

    private void openSearch() {
        layoutSearch.setVisibility(View.VISIBLE);
        etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeSearch() {
        etSearch.setText("");
        layoutSearch.setVisibility(View.GONE);
        showFeed();
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    private void showFeed() {
        rvSearchResults.setVisibility(View.GONE);
        rvFeed.setVisibility(View.VISIBLE);
        searchResults.clear();
        searchAdapter.notifyDataSetChanged();
    }

    private void searchUsers(String query) {
        String q = query.toLowerCase();
        db.collection("users").limit(100).get()
                .addOnSuccessListener(snap -> {
                    searchResults.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String uid = doc.getString("userId");
                        if (uid != null && uid.equals(userId)) continue;
                        String first = doc.getString("firstName");
                        String last  = doc.getString("surname");
                        String full  = ((first != null ? first : "") + " "
                                + (last != null ? last : "")).toLowerCase().trim();
                        if (full.contains(q)) searchResults.add(doc);
                    }
                    searchAdapter.notifyDataSetChanged();
                    rvFeed.setVisibility(View.GONE);
                    rvSearchResults.setVisibility(View.VISIBLE);
                });
    }

    private void loadFeed() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snap -> {
                    postList.clear();
                    postList.addAll(snap.getDocuments());
                    adapter.resetAnimations();
                    adapter.notifyDataSetChanged();
                });
    }

    private void openCommentSheet(String postId, TextView tvCommentCount) {
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
                new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        rv.setAdapter(commentAdapter);

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
                    tvCommentCount.setText(String.valueOf(commentList.size()));
                    if (!commentList.isEmpty())
                        rv.scrollToPosition(commentList.size() - 1);
                });

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
