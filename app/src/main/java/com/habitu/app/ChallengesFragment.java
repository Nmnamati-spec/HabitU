package com.habitu.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class ChallengesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenges,
                container, false);
        LinearLayout container2 = view.findViewById(R.id.challengesContainer);

        String[][] challenges = {
                {"🏃", "30-Day Running Streak",    "Run every day for 30 days",  "30 days",  "🥇"},
                {"📚", "Study 2hrs Daily",         "Study 2 hours every day",    "14 days",  "📖"},
                {"💧", "Hydration Challenge",      "Drink 8 glasses daily",      "7 days",   "💧"},
                {"🧘", "Mindfulness Month",        "Meditate every morning",     "30 days",  "🧘"},
                {"💪", "Gym 3x a Week",            "Hit the gym 3 times weekly", "4 weeks",  "💪"},
        };

        for (String[] c : challenges) {
            View card = createChallengeCard(c[0], c[1], c[2], c[3], c[4]);
            container2.addView(card);
        }

        return view;
    }

    private View createChallengeCard(String icon, String title,
                                     String desc, String duration, String badge) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.input_bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 14);
        card.setLayoutParams(params);
        card.setPadding(36, 32, 36, 32);

        LinearLayout topRow = new LinearLayout(getContext());
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvIcon = new TextView(getContext());
        tvIcon.setText(icon + "  ");
        tvIcon.setTextSize(22);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title);
        tvTitle.setTextSize(15);
        tvTitle.setTextColor(0xFFF4F5F0);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvBadge = new TextView(getContext());
        tvBadge.setText(badge);
        tvBadge.setTextSize(20);

        topRow.addView(tvIcon);
        topRow.addView(tvTitle);
        topRow.addView(tvBadge);

        TextView tvDesc = new TextView(getContext());
        tvDesc.setText(desc);
        tvDesc.setTextSize(12);
        tvDesc.setTextColor(0xFF6B6D65);
        tvDesc.setPadding(0, 6, 0, 6);

        LinearLayout bottomRow = new LinearLayout(getContext());
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setPadding(0, 8, 0, 0);

        TextView tvDuration = new TextView(getContext());
        tvDuration.setText("⏱ " + duration);
        tvDuration.setTextSize(11);
        tvDuration.setTextColor(0xFF6B6D65);
        tvDuration.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvJoin = new TextView(getContext());
        tvJoin.setText("Join Challenge →");
        tvJoin.setTextSize(12);
        tvJoin.setTextColor(0xFFC8F53B);
        tvJoin.setTypeface(null, android.graphics.Typeface.BOLD);
        tvJoin.setOnClickListener(v ->
                Toast.makeText(getContext(),
                        "Joined: " + title + " 🏆", Toast.LENGTH_SHORT).show());

        bottomRow.addView(tvDuration);
        bottomRow.addView(tvJoin);

        card.addView(topRow);
        card.addView(tvDesc);
        card.addView(bottomRow);

        return card;
    }
}
