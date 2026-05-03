package com.habitu.app;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("REMINDER_TEST", "Receiver triggered");

        // Reschedule for tomorrow if triggered by the alarm
        if (intent.getBooleanExtra("is_auto_reschedule", false) || true) {
            scheduleNext(context);
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .collection("logs").get()
                .addOnSuccessListener(snap -> {
                    checkAndNotify(context, snap.getDocuments());
                });
    }

    private void checkAndNotify(Context context, List<DocumentSnapshot> docs) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String today = sdf.format(new Date());
        boolean doneToday = false;

        Set<String> loggedDays = new HashSet<>();
        for (DocumentSnapshot doc : docs) {
            Long ts = doc.getLong("timestamp");
            if (ts != null) {
                String day = sdf.format(new Date(ts));
                loggedDays.add(day);
                if (day.equals(today)) {
                    doneToday = true;
                }
            }
        }

        if (!doneToday) {
            int streak = calculateStreak(loggedDays);
            sendNotification(context, streak);
        }
    }

    private int calculateStreak(Set<String> loggedDays) {
        int streak = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        // Start from yesterday for the streak calculation in the notification
        // since we already know today is not logged.
        cal.add(Calendar.DAY_OF_YEAR, -1);

        for (int i = 0; i < 365; i++) {
            String day = sdf.format(cal.getTime());
            if (loggedDays.contains(day)) {
                streak++;
            } else {
                break;
            }
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    private void sendNotification(Context context, int streak) {
        String title = streak > 0 ? "🔥 " + streak + "-day streak!" : "Don't forget your habits!";
        String text = streak > 0 ? "Don't break your streak today!" : "Complete your tasks to start a streak!";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "channel_id")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(1, builder.build());
    }

    private void scheduleNext(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("is_auto_reschedule", true);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 20); // 8 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long triggerTime = calendar.getTimeInMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}
