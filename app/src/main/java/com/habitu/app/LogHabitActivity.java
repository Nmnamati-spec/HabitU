package com.habitu.app;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LogHabitActivity extends AppCompatActivity {

    Spinner spinnerHabitType;
    EditText etDuration, etDistance, etNotes;
    Button btnSaveLog;
    FirebaseFirestore db;
    String userId;

    String[] habitTypes = {
            "🏃 Running", "📚 Studying", "💪 Gym",
            "🧘 Meditation", "🥗 Nutrition", "💧 Hydration",
            "📖 Reading", "😴 Sleep", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_habit);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        spinnerHabitType = findViewById(R.id.spinnerHabitType);
        etDuration       = findViewById(R.id.etDuration);
        etDistance       = findViewById(R.id.etDistance);
        etNotes          = findViewById(R.id.etNotes);
        btnSaveLog       = findViewById(R.id.btnSaveLog);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, habitTypes);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerHabitType.setAdapter(adapter);

        btnSaveLog.setOnClickListener(v -> saveLog());
    }

    private void saveLog() {
        String habit    = spinnerHabitType.getSelectedItem().toString();
        String duration = etDuration.getText().toString().trim();
        String distance = etDistance.getText().toString().trim();
        String notes    = etNotes.getText().toString().trim();

        if (duration.isEmpty()) {
            etDuration.setError("Please enter duration");
            return;
        }

        btnSaveLog.setEnabled(false);
        btnSaveLog.setText("Saving...");

        Calendar cal = Calendar.getInstance();
        int week  = cal.get(Calendar.WEEK_OF_YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day   = cal.get(Calendar.DAY_OF_MONTH);

        Map<String, Object> log = new HashMap<>();
        log.put("habit",     habit);
        log.put("duration",  Integer.parseInt(duration));
        log.put("distance",  distance.isEmpty() ? 0 :
                Double.parseDouble(distance));
        log.put("notes",     notes);
        log.put("timestamp", System.currentTimeMillis());
        log.put("week",      week);
        log.put("month",     month);
        log.put("day",       day);
        log.put("userId",    userId);

        db.collection("users").document(userId)
                .collection("logs").add(log)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this,
                            "Habit logged! Keep going 🌱",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnSaveLog.setEnabled(true);
                    btnSaveLog.setText("Save to Dashboard 🌱");
                });
    }
}
