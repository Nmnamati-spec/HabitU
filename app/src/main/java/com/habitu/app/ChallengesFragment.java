package com.habitu.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        // {iconRes, title, desc, duration, badgeRes}
        Object[][] challenges = {
                {R.drawable.ic_run,       "30-Day Running Streak",  "Run every day for 30 days",  "30 days", R.drawable.ic_medal},
                {R.drawable.ic_study,     "Study 2hrs Daily",        "Study 2 hours every day",    "14 days", R.drawable.ic_trophy},
                {R.drawable.ic_water,     "Hydration Challenge",     "Drink 8 glasses daily",      "7 days",  R.drawable.ic_target},
                {R.drawable.ic_meditation,"Mindfulness Month",       "Meditate every morning",     "30 days", R.drawable.ic_streak},
                {R.drawable.ic_gym,       "Gym 3x a Week",           "Hit the gym 3 times weekly", "4 weeks", R.drawable.ic_trophy},
        };

        for (Object[] c : challenges) {
            View card = createChallengeCard(
                    (int) c[0], (String) c[1], (String) c[2],
                    (String) c[3], (int) c[4]);
            container2.addView(card);
        }

        return view;
    }

    private View createChallengeCard(int iconRes, String title,
                                     String desc, String duration, int badgeRes) {
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
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView imgIcon = new ImageView(getContext());
        imgIcon.setImageResource(iconRes);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(48, 48);
        iconParams.setMarginEnd(16);
        imgIcon.setLayoutParams(iconParams);
        imgIcon.setPadding(8, 8, 8, 8);
        imgIcon.setBackgroundResource(R.drawable.icon_container_bg);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title);
        tvTitle.setTextSize(15);
        tvTitle.setTextColor(0xFFF4F5F0);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        ImageView imgBadge = new ImageView(getContext());
        imgBadge.setImageResource(badgeRes);
        imgBadge.setLayoutParams(new LinearLayout.LayoutParams(36, 36));

        topRow.addView(imgIcon);
        topRow.addView(tvTitle);
        topRow.addView(imgBadge);

        TextView tvDesc = new TextView(getContext());
        tvDesc.setText(desc);
        tvDesc.setTextSize(12);
        tvDesc.setTextColor(0xFF6B6D65);
        tvDesc.setPadding(0, 6, 0, 6);

        LinearLayout bottomRow = new LinearLayout(getContext());
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setPadding(0, 8, 0, 0);
        bottomRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView imgTimer = new ImageView(getContext());
        imgTimer.setImageResource(R.drawable.ic_timer);
        LinearLayout.LayoutParams timerParams = new LinearLayout.LayoutParams(16, 16);
        timerParams.setMarginEnd(6);
        imgTimer.setLayoutParams(timerParams);

        TextView tvDuration = new TextView(getContext());
        tvDuration.setText(duration);
        tvDuration.setTextSize(11);
        tvDuration.setTextColor(0xFF6B6D65);
        tvDuration.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvJoin = new TextView(getContext());
        tvJoin.setText("Join Challenge →");
        tvJoin.setTextSize(12);
        tvJoin.setTextColor(0xFFD4A832);
        tvJoin.setTypeface(null, android.graphics.Typeface.BOLD);
        tvJoin.setOnClickListener(v ->
                Toast.makeText(getContext(),
                        "Joined: " + title, Toast.LENGTH_SHORT).show());

        bottomRow.addView(imgTimer);
        bottomRow.addView(tvDuration);
        bottomRow.addView(tvJoin);

        card.addView(topRow);
        card.addView(tvDesc);
        card.addView(bottomRow);

        return card;
    }
}
