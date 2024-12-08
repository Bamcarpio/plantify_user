package com.example.plantify_user.ui;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.example.plantify_user.domain.Plant;
import com.example.plantify_user.domain.PlantDBHandler;

public class HomeDetailsSharedViewModel extends ViewModel {
    MutableLiveData<List<Plant>> plants;

    public LiveData<List<Plant>> getPlants(Context context) {
        if (plants == null) {
            plants = new MutableLiveData<>();
        }
        loadPlants(context);
        return plants;
    }

    private void loadPlants(Context context) {
        PlantDBHandler plantDBHandler = new PlantDBHandler(context);
        plants.setValue(plantDBHandler.getAllPlants());
    }

    public void deletePlant(int position, Context context) {
        long id = Objects.requireNonNull(plants.getValue()).get(position).getId();
        PlantDBHandler plantDBHandler = new PlantDBHandler(context);
        plantDBHandler.removePlant(id);
    }

    public void waterPlant(int position, Context context) {
        PlantDBHandler plantDBHandler = new PlantDBHandler(context);

        // Fetch the plant to update
        Plant plant = Objects.requireNonNull(plants.getValue()).get(position);

        // Update the plant's properties
        plant.setLastWatered(LocalDateTime.now());
        plant.setNotificationSent(false); // Reset the notification flag

        // Persist the updated plant back to the database
        plantDBHandler.updatePlant(plant);

        // Update the LiveData to reflect the changes
        plants.getValue().set(position, plant);
        plants.postValue(plants.getValue());
    }
}
