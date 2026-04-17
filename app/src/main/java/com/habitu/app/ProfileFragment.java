package com.habitu.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,
                container, false);

        TextView tvName       = view.findViewById(R.id.tvProfileName);
        TextView tvUniversity = view.findViewById(R.id.tvProfileUniversity);
        Button   btnSignOut   = view.findViewById(R.id.btnSignOut);

        // Load user data from Firestore
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String first = doc.getString("firstName");
                        String last  = doc.getString("surname");
                        String uni   = doc.getString("university");
                        tvName.setText(first + " " + last);
                        tvUniversity.setText(
                                uni != null && !uni.isEmpty() ? uni : "University not set");
                    }
                });

        // Sign out
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });

        return view;
    }
}