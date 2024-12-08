package com.example.plantify_user.domain;

import java.time.Duration;
import java.time.LocalDateTime;


public class PlantUtils {

    public static Duration timeToNextWatering(Plant plant) {
        Duration timeSinceLast = Duration.between(plant.getLastWatered(), LocalDateTime.now());


        return plant.getWateringInterval().minus(timeSinceLast);
    }
    public static Duration calculateAge(Plant plant) {
        return plant.getBirthday()
                .map((birthday -> Duration.between(birthday, LocalDateTime.now())))
                .orElse(null);
    }
}
