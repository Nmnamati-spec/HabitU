package com.habitu.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class HabitsFragment extends Fragment {

    TextView tvTodayDate, tvStreakCount, tvWeeklyScore,
            tvTotalLogs, tabWeekly, tabMonthly, btnLogNewHabit;
    LinearLayout logsContainer;
    BarChart barChart;
    FirebaseFirestore db;
    String userId;
    boolean showingWeekly = true;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habits,
                container, false);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvTodayDate    = view.findViewById(R.id.tvTodayDate);
        tvStreakCount  = view.findViewById(R.id.tvStreakCount);
        tvWeeklyScore  = view.findViewById(R.id.tvWeeklyScore);
        tvTotalLogs    = view.findViewById(R.id.tvTotalLogs);
        tabWeekly      = view.findViewById(R.id.tabWeekly);
        tabMonthly     = view.findViewById(R.id.tabMonthly);
        btnLogNewHabit = view.findViewById(R.id.btnLogNewHabit);
        logsContainer  = view.findViewById(R.id.logsContainer);
        barChart       = view.findViewById(R.id.barChart);

        String date = new SimpleDateFormat(
                "EEEE, d MMMM yyyy", Locale.getDefault())
                .format(new Date());
        tvTodayDate.setText(date);

        btnLogNewHabit.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), LogHabitActivity.class)));

        tabWeekly.setOnClickListener(v -> {
            showingWeekly = true;
            tabWeekly.setBackgroundResource(R.drawable.btn_lime);
            tabWeekly.setTextColor(Color.parseColor("#0E0F0C"));
            tabMonthly.setBackgroundResource(R.drawable.input_bg);
            tabMonthly.setTextColor(Color.parseColor("#6B6D65"));
            loadWeeklyChart();
        });

        tabMonthly.setOnClickListener(v -> {
            showingWeekly = false;
            tabMonthly.setBackgroundResource(R.drawable.btn_lime);
            tabMonthly.setTextColor(Color.parseColor("#0E0F0C"));
            tabWeekly.setBackgroundResource(R.drawable.input_bg);
            tabWeekly.setTextColor(Color.parseColor("#6B6D65"));
            loadMonthlyChart();
        });

        loadStats();
        loadWeeklyChart();
        loadRecentLogs();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
        loadRecentLogs();
        if (showingWeekly) loadWeeklyChart();
        else loadMonthlyChart();
    }

    private void loadStats() {
        Calendar cal  = Calendar.getInstance();
        int thisWeek  = cal.get(Calendar.WEEK_OF_YEAR);

        db.collection("users").document(userId)
                .collection("logs").get()
                .addOnSuccessListener(snap -> {
                    tvTotalLogs.setText(String.valueOf(snap.size()));
                    int streak = calculateStreak(snap.getDocuments());
                    tvStreakCount.setText(String.valueOf(streak));
                });

        db.collection("users").document(userId)
                .collection("logs")
                .whereEqualTo("week", thisWeek)
                .get()
                .addOnSuccessListener(snap -> {
                    int score = Math.min(snap.size() * 14, 100);
                    tvWeeklyScore.setText(score + "%");
                });
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
            String day = sdf.format(cal.getTime());
            if (loggedDays.contains(day)) streak++;
            else if (i > 0) break;
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    private void loadWeeklyChart() {
        int thisWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

        db.collection("users").document(userId)
                .collection("logs")
                .whereEqualTo("week", thisWeek)
                .get()
                .addOnSuccessListener(snap -> {
                    float[] counts = new float[7];
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Long ts = doc.getLong("timestamp");
                        if (ts != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(ts);
                            int dow = cal.get(Calendar.DAY_OF_WEEK) - 2;
                            if (dow < 0) dow = 6;
                            if (dow < 7) counts[dow]++;
                        }
                    }
                    renderChart(counts, days);
                });
    }

    private void loadMonthlyChart() {
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        String[] weeks = {"W1","W2","W3","W4"};

        db.collection("users").document(userId)
                .collection("logs")
                .whereEqualTo("month", thisMonth)
                .get()
                .addOnSuccessListener(snap -> {
                    float[] counts = new float[4];
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Long ts = doc.getLong("timestamp");
                        if (ts != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(ts);
                            int wom = cal.get(Calendar.WEEK_OF_MONTH) - 1;
                            if (wom >= 0 && wom < 4) counts[wom]++;
                        }
                    }
                    renderChart(counts, weeks);
                });
    }

    private void renderChart(float[] values, String[] labels) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            entries.add(new BarEntry(i, values[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Habits");
        dataSet.setColor(Color.parseColor("#D4A832"));
        dataSet.setValueTextColor(Color.parseColor("#F4F5F0"));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        barChart.setData(data);

        barChart.setBackgroundColor(Color.parseColor("#222420"));
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.animateY(800);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#6B6D65"));
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        barChart.getAxisLeft().setTextColor(Color.parseColor("#6B6D65"));
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    private void loadRecentLogs() {
        db.collection("users").document(userId)
                .collection("logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snap -> {
                    logsContainer.removeAllViews();
                    if (snap.isEmpty()) {
                        TextView empty = new TextView(getContext());
                        empty.setText("No habits logged yet. Tap Log to start!");
                        empty.setTextColor(0xFF6B6D65);
                        empty.setTextSize(13);
                        empty.setPadding(0, 16, 0, 0);
                        logsContainer.addView(empty);
                        return;
                    }
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        addLogCard(doc);
                    }
                });
    }

    private void addLogCard(DocumentSnapshot doc) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.item_habit_log, logsContainer, false);

        String habit    = doc.getString("habit");
        Long duration   = doc.getLong("duration");
        Double distance = doc.getDouble("distance");
        Long ts         = doc.getLong("timestamp");

        ImageView imgHabitIcon = card.findViewById(R.id.imgHabitIcon);
        TextView tvName        = card.findViewById(R.id.tvHabitName);
        TextView tvMeta        = card.findViewById(R.id.tvHabitMeta);
        TextView tv1Val        = card.findViewById(R.id.tvStat1Val);
        TextView tv1Lbl        = card.findViewById(R.id.tvStat1Lbl);
        TextView tv2Val        = card.findViewById(R.id.tvStat2Val);
        TextView tv2Lbl        = card.findViewById(R.id.tvStat2Lbl);
        TextView tv3Val        = card.findViewById(R.id.tvStat3Val);
        TextView tv3Lbl        = card.findViewById(R.id.tvStat3Lbl);

        imgHabitIcon.setImageResource(HabitIconMapper.getIcon(habit));
        tvName.setText(habit != null ? habit : "Habit");

        String date = ts != null ? new SimpleDateFormat(
                "EEE, d MMM", Locale.getDefault()).format(new Date(ts)) : "";
        tvMeta.setText(date);

        tv1Val.setText(duration != null ? duration + "min" : "—");
        tv1Lbl.setText("Duration");

        if (distance != null && distance > 0) {
            tv2Val.setText(distance + "km");
            tv2Lbl.setText("Distance");
        } else {
            tv2Val.setText("—");
            tv2Lbl.setText("Distance");
        }

        tv3Val.setText("Done");
        tv3Val.setTextColor(Color.parseColor("#D4A832"));
        tv3Lbl.setText("Status");

        logsContainer.addView(card);
    }
}
