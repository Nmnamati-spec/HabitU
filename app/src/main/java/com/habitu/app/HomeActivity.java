package com.habitu.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.PathInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    FloatingActionButton fabUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        fabUpload = findViewById(R.id.fabUpload);

        fabUpload.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, UploadPostActivity.class)));

        // Show FAB on Feed, hide everywhere else
        loadFragment(new FeedFragment(), true);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selected;
            boolean isFeed = id == R.id.nav_feed;

            if (id == R.id.nav_feed) {
                selected = new FeedFragment();
            } else if (id == R.id.nav_habits) {
                selected = new HabitsFragment();
            } else if (id == R.id.nav_communities) {
                selected = new CommunitiesFragment();
            } else if (id == R.id.nav_challenges) {
                selected = new ChallengesFragment();
            } else {
                selected = new ProfileFragment();
            }

            loadFragment(selected, isFeed);
            return true;
        });
    }

    private void loadFragment(Fragment fragment, boolean showFab) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        if (showFab) {
            fabUpload.setScaleX(0.72f);
            fabUpload.setScaleY(0.72f);
            fabUpload.setAlpha(0f);
            fabUpload.setVisibility(View.VISIBLE);
            fabUpload.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(250)
                    .setInterpolator(new PathInterpolator(0.23f, 1f, 0.32f, 1f))
                    .start();
        } else if (fabUpload.getVisibility() == View.VISIBLE) {
            fabUpload.animate()
                    .scaleX(0.72f)
                    .scaleY(0.72f)
                    .alpha(0f)
                    .setDuration(180)
                    .setInterpolator(new PathInterpolator(0.55f, 0f, 1f, 0.45f))
                    .withEndAction(() -> fabUpload.setVisibility(View.GONE))
                    .start();
        }
    }
}
