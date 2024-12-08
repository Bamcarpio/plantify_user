package com.example.plantify_user.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.example.plantify_user.R;
import com.example.plantify_user.domain.Plant;
import com.example.plantify_user.domain.PlantUtils;


public class PlantFormatter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT); // 5/14/21, 5:59 PM
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofLocalizedDate(FormatStyle.SHORT); // 5/14/21

    private static final String CHANNEL_ID = "plant_notification_channel";
    private final Context context;
    private static final String NOTIFICATION_SENT_FLAG = "notification_sent_flag";
    private final Plant plant;
    private static Bitmap DEFAULT_PHOTO_BITMAP = null;
    private final Resources resources;

    private static final int NOTIFICATION_ID = 1;

    public PlantFormatter(Context context, Plant plant) {
        this.context = Objects.requireNonNull(context);
        this.resources = context.getResources();
        this.plant = Objects.requireNonNull(plant);

        if (DEFAULT_PHOTO_BITMAP == null) {
            DEFAULT_PHOTO_BITMAP = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_plant_image);
        }

        scheduleNotification();
    }

    private void scheduleNotification() {

        Duration timeToNextWatering = PlantUtils.timeToNextWatering(plant);
        long delayMillis = timeToNextWatering.toMillis();


        String plantPhotoPath = null;
        if (plant.getPhoto().isPresent()) {
            byte[] photoBytes = plant.getPhoto().get();
            try {
                plantPhotoPath = savePhotoToFile(photoBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Data.Builder inputDataBuilder = new Data.Builder()
                .putString("plantName", plant.getName())
                .putLong("plantId", plant.getId());

        if (plantPhotoPath != null) {
            inputDataBuilder.putString("plantPhotoPath", plantPhotoPath);
        }

        Data inputData = inputDataBuilder.build();


        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();


        WorkManager.getInstance(context).enqueue(notificationWork);
    }


    public String timeToNextWatering() {
        Duration timeToNext = PlantUtils.timeToNextWatering(plant);

        if (timeToNext.isNegative() || timeToNext.isZero()) {
            return context.getResources().getString(R.string.formatter_water_now_message);
        } else if (timeToNext.minus(Duration.ofMinutes(1)).isNegative()) {
            return context.getResources().getString(R.string.time_to_next_under_minute);
        }

        return formattedDuration(timeToNext);
    }


    private String savePhotoToFile(byte[] photoBytes) throws IOException {
        File photoFile = new File(context.getFilesDir(), "plant_photo_" + plant.getId() + ".png");
        try (FileOutputStream fos = new FileOutputStream(photoFile)) {
            fos.write(photoBytes);
        }
        return photoFile.getAbsolutePath();
    }


    public String age() {
        Duration age = PlantUtils.calculateAge(plant);
        if (Objects.isNull(age)) {

            return resources.getString(R.string.plant_no_age_message);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (age.minusDays(1).minusSeconds(1).isNegative()) {

                return resources.getString(R.string.plant_very_young_message);
            }
        }


        return formattedDuration(age, TimespanUnits.DAYS);
    }

    public long id() {
        return plant.getId();
    }

    public String name() {
        return plant.getName();
    }

    public String birthday() {
        if (plant.getBirthday().isPresent()) {
            return formattedDateTime(plant.getBirthday().get());
        }
        return resources.getString(R.string.plant_no_age_message);
    }

    public String lastWatered() {
        return formattedDateTime(plant.getLastWatered());
    }

    public String wateringInterval() {
        return formattedDuration(plant.getWateringInterval());
    }


    private String formattedDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    private String formattedDate(LocalDateTime date) {
        return date.format(DATE_FORMATTER);
    }
    private String formattedDuration(Duration duration, TimespanUnits minUnit) {
        StringBuilder result = new StringBuilder();


        final List<TimespanUnits> timeUnits = Arrays.stream(TimespanUnits.values())
                .filter(unit -> unit.getMinutesInThis() >= minUnit.getMinutesInThis())
                .sorted(TimespanUnits.descendingOrder)
                .collect(Collectors.toCollection(ArrayList::new));


        final String[] labelsSingular = resources.getStringArray(R.array.duration_formatter_YMDhm_labels_singular);
        final String[] labelsPlural = resources.getStringArray(R.array.duration_formatter_YMDhm_labels_plural);

        if (labelsSingular.length != labelsPlural.length || labelsSingular.length < timeUnits.size()) {

            throw new IllegalStateException("Label/TimespanUnit matching problem");
        }

        Duration durationLeft = Duration.from(duration);
        boolean addLeadingSpace = false;

        for (int i = 0; i < timeUnits.size(); i++) {
            TimespanUnits currentTimespanUnit = timeUnits.get(i);
            long durationLeftMinutes = durationLeft.toMinutes();
            long currentUnitsLeft = currentTimespanUnit.fromMinutes(durationLeftMinutes);
            if (currentUnitsLeft == 0) continue;


            if (addLeadingSpace) result.append(' ');
            result.append(currentUnitsLeft);
            result.append(' ');
            result.append(currentUnitsLeft == 1 ? labelsSingular[i] : labelsPlural[i]);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                durationLeft = durationLeft.minusMinutes(
                        currentTimespanUnit.toMinutes(currentUnitsLeft));
            }

            if (durationLeft.toMinutes() <= 0) {
                break;
            }
            addLeadingSpace = true;
        }

        return result.toString();
    }
    private String formattedDuration(Duration duration) {
        return formattedDuration(duration, TimespanUnits.MINUTES);
    }
    public Bitmap photo() {
        if (plant.getPhoto().isPresent()) {
            byte[] blob = plant.getPhoto().get();
            Bitmap nullableBitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            if (Objects.nonNull(nullableBitmap)) {
                return nullableBitmap;
            }
        }
        return PlantFormatter.DEFAULT_PHOTO_BITMAP;
    }



    public static class BitmapEncoding {
        public static final Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        public static final int quality = 100;
    }
    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ID = ");
        builder.append(id());

        builder.append(", name = ");
        builder.append(name());


        builder.append(", birthday = ");
        if (plant.getBirthday().isPresent()) {
            builder.append(birthday());
        } else {
            builder.append("null");
        }

        builder.append(", last_watered = ");
        builder.append(lastWatered());

        builder.append(", watering_interval = ");
        builder.append(wateringInterval());


        builder.append(", has_photo = ");
        builder.append(plant.getPhoto().isPresent());

        return builder.toString();
    }
}



