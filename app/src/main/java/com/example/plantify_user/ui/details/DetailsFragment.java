package com.example.plantify_user.ui.details;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.plantify_user.domain.PlantUtils;
import com.example.plantify_user.ui.FirstActivity;
import com.example.plantify_user.ui.NotificationWorker;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.example.plantify_user.R;
import com.example.plantify_user.domain.Plant;
import com.example.plantify_user.ui.PlantFormatter;
import com.example.plantify_user.ui.HomeDetailsSharedViewModel;

public class DetailsFragment extends Fragment {

    private Handler handler;
    private static final String NOTIFICATION_SENT_FLAG_PREFIX = "notification_sent_flag_";

    private Runnable updateTask;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_details, container, false);
        HomeDetailsSharedViewModel sharedViewModel =
                new ViewModelProvider(requireActivity()).get(HomeDetailsSharedViewModel.class);
        Context context = getContext();

        assert getArguments() != null;
        int position = getArguments().getInt("position");
        Plant current_plant =
                Objects.requireNonNull(sharedViewModel.getPlants(context).getValue()).get(position);
        PlantFormatter plantFormatter = new PlantFormatter(context, current_plant);


        TextView name = root.findViewById(R.id.textViewDetails2);
        ImageView plant_image = root.findViewById(R.id.imageViewDetails1);
        TextView next_watering = root.findViewById(R.id.textViewDetails4);
        TextView age = root.findViewById(R.id.textViewDetails6);
        name.setText(plantFormatter.name());
        plant_image.setImageBitmap(plantFormatter.photo());
        next_watering.setText(plantFormatter.timeToNextWatering());
        age.setText(plantFormatter.age());


        handler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                // Update the next watering text
                next_watering.setText(plantFormatter.timeToNextWatering());
                // Schedule the next update after 1 second (1000 milliseconds)
                handler.postDelayed(this, 1000);
            }
        };


        handler.post(updateTask);


        Button delete_button = root.findViewById(R.id.buttonDetails2);


        Button just_watered_button = root.findViewById(R.id.buttonDetails1);

        delete_button.setOnClickListener(v ->
                deleteWithConfirmation(
                        context, sharedViewModel, position, delete_button, just_watered_button));

        just_watered_button.setOnClickListener(v -> {

            sharedViewModel.waterPlant(position, context);


            rescheduleNotification(current_plant, context);


            next_watering.setText(plantFormatter.timeToNextWatering());
            Snackbar.make(this.requireView(), R.string.details_just_watered_response, Snackbar.LENGTH_SHORT).show();
            just_watered_button.setEnabled(false);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (handler != null) {
            handler.removeCallbacks(updateTask);
        }
    }
    private String savePhotoToFile(byte[] photoBytes, long plantId, Context context) throws IOException {
        File photoFile = new File(context.getFilesDir(), "plant_photo_" + plantId + ".png");
        try (FileOutputStream fos = new FileOutputStream(photoFile)) {
            fos.write(photoBytes);
        }
        return photoFile.getAbsolutePath();
    }


    private void deleteWithConfirmation(Context context, HomeDetailsSharedViewModel sharedViewModel,
                                        int position,
                                        Button delete_button, Button just_watered_button) {
        new AlertDialog.Builder(this.requireActivity())
                .setMessage(R.string.details_delete_plant_dialogue)
                .setPositiveButton(R.string.yes, (d, w) -> {

                    Plant plantToDelete = sharedViewModel.getPlants(context).getValue().get(position);
                    rescheduleNotification(plantToDelete, context);


                    sharedViewModel.deletePlant(position, context);
                    Snackbar.make(this.requireView(), R.string.details_delete_success_response,
                            Snackbar.LENGTH_SHORT).show();
                    delete_button.setEnabled(false);


                    just_watered_button.setEnabled(false);
                })
                .setNegativeButton(R.string.no, (d, w) -> {

                })
                .show();
    }

    private void rescheduleNotification(Plant plant, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("plant_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String flagKey = NOTIFICATION_SENT_FLAG_PREFIX + plant.getId();
        editor.putBoolean(flagKey, false);
        editor.apply();
        Log.d("DetailsFragment", "Reset notification flag for plant: " + plant.getId());


        Duration timeToNextWatering = PlantUtils.timeToNextWatering(plant);
        long delayMillis = timeToNextWatering.toMillis();

        if (delayMillis <= 0) {
            Log.d("DetailsFragment", "Next watering is already due. Not scheduling notification.");
            return;
        }

        Data inputData = new Data.Builder()
                .putString("plantName", plant.getName())
                .putLong("plantId", plant.getId())
                .build();

        WorkManager.getInstance(context).cancelAllWorkByTag("plant_" + plant.getId());

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("plant_" + plant.getId())
                .build();

        WorkManager.getInstance(context).enqueue(notificationWork);
        Log.d("DetailsFragment", "Scheduled new notification for plant: " + plant.getId() + ", delay: " + delayMillis + "ms");
    }
}










