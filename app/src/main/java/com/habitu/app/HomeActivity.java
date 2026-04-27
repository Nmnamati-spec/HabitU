package com.habitu.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        FloatingActionButton fabUpload = findViewById(R.id.fabUpload);

        fabUpload.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, UploadPostActivity.class)));

        // Load Feed as the first screen
        loadFragment(new FeedFragment());

        // Handle tab switching
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();

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

            loadFragment(selected);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}