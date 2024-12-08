package com.example.plantify_user.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.plantify_user.MainActivity;
import com.example.plantify_user.R;
import com.example.plantify_user.domain.Plant;
import com.example.plantify_user.domain.PlantDBHandler;
import com.example.plantify_user.domain.PlantUtils;

import java.time.Duration;
import java.util.Optional;


public class NotificationWorker extends Worker {

    private static final String NOTIFICATION_SENT_FLAG_PREFIX = "notification_sent_flag_";
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        String plantName = getInputData().getString("plantName");
        long plantId = getInputData().getLong("plantId", -1);
        String plantPhotoPath = getInputData().getString("plantPhotoPath");

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("plant_preferences", Context.MODE_PRIVATE);
        boolean notificationSent = sharedPreferences.getBoolean(NOTIFICATION_SENT_FLAG_PREFIX + plantId, false);


        PlantDBHandler plantDBHandler = new PlantDBHandler(getApplicationContext());
        Optional<Plant> plantOpt = plantDBHandler.getPlantById(plantId); // Fetch using the Optional return type

        if (!plantOpt.isPresent()) {
            Log.e("NotificationWorker", "Plant not found for ID: " + plantId);
            return Result.failure();
        }

        Plant plant = plantOpt.get();

        Duration timeToNextWatering = PlantUtils.timeToNextWatering(plant);

        if (!notificationSent && (timeToNextWatering.isZero() || timeToNextWatering.isNegative())) {
            sendNotification(plantName, plantId, BitmapFactory.decodeFile(plantPhotoPath));


            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(NOTIFICATION_SENT_FLAG_PREFIX + plantId, true);
            editor.apply();
        } else {
            Log.d("NotificationWorker", "Notification not sent. Time to next watering: " + timeToNextWatering);
        }

        return Result.success();
    }


    private void sendNotification(String plantName, long plantId, Bitmap plantPhoto) {
        Context context = getApplicationContext();
        String CHANNEL_ID = "plant_notification_channel";
        int NOTIFICATION_ID = (int) plantId;


        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra("plant_id", plantId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );


        createNotificationChannel(context, CHANNEL_ID);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.plant_logo)
                .setContentTitle("Water Your Plant!")
                .setContentText("It's time to water your " + plantName + " plant.")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set higher priority for alarm-like behavior
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        if (plantPhoto != null) {
            builder.setLargeIcon(plantPhoto);
        }

        // Display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) != null) {
                notificationManager.deleteNotificationChannel(channelId);
            }


            Uri alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);


            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Plant Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for plant watering reminders");
            channel.setSound(alarmSoundUri, new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build());

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


}



