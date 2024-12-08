package com.example.plantify_user.plantNotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.plantify_user.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmId = intent.getStringExtra("ALARM_ID");
        String alarmName = intent.getStringExtra("ALARM_NAME");

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alarm_channel",
                    "Alarm Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Build and show notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.baseline_book_24)
                .setContentTitle("Alarm: " + alarmName)
                .setContentText("Time to water your plants!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(alarmId.hashCode(), builder.build());
        } else {
            Log.e("AlarmReceiver", "Notification permission not granted.");
        }

        // Play alarm sound
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            if (mediaPlayer != null) {
                mediaPlayer.start();

                // Stop sound after 30 seconds
                new Handler(Looper.getMainLooper()).postDelayed(mediaPlayer::release, 30000);
            } else {
                Log.e("AlarmReceiver", "MediaPlayer creation failed.");
            }
        } catch (Exception e) {
            Log.e("AlarmReceiver", "Error playing alarm sound", e);
        }
    }
}
