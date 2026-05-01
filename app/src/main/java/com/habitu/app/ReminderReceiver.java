package com.habitu.app;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs =
                context.getSharedPreferences("habit", Context.MODE_PRIVATE);

        boolean checked = prefs.getBoolean("checkedToday", false);

        if (!checked) {

            int streak = prefs.getInt("streak", 0);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, "channel_id")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("🔥 " + streak + "-day streak!")
                            .setContentText("Don't break it today!")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            manager.notify(1, builder.build());
        }

        prefs.edit().putBoolean("checkedToday", false).apply();
    }
}