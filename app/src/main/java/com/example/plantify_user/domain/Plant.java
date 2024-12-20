package com.example.plantify_user.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


public class Plant {

    private long id;
    private String name;
    private LocalDateTime birthday;
    private LocalDateTime lastWatered;
    private Duration wateringInterval;
    private byte[] photo;
    private boolean notificationSent;

    public Plant() {

        id = ThreadLocalRandom.current()
                .nextLong(100_000_000L, Long.MAX_VALUE);

        name = "John Doe";
        lastWatered = LocalDateTime.now();
        wateringInterval = Duration.ofDays(1);
        notificationSent = false;
    }


    Plant(long id, String name, LocalDateTime birthday,
          LocalDateTime lastWatered, Duration wateringInterval,
          byte[] photo) {

        setId(id);
        setName(name);
        setBirthday(birthday);
        setLastWatered(lastWatered);
        setWateringInterval(wateringInterval);
        setPhoto(photo);
        notificationSent = false;
    }

    public Plant(String name, LocalDateTime birthday,
                 LocalDateTime lastWatered, Duration wateringInterval,
                 byte[] photo) {

        this(-1, name, birthday, lastWatered, wateringInterval, photo);
    }

    // Getters and setters

    public long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public Optional<LocalDateTime> getBirthday() {
        return Optional.ofNullable(birthday);
    }

    public final void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public LocalDateTime getLastWatered() {
        return lastWatered;
    }

    public final void setLastWatered(LocalDateTime lastWatered) {
        this.lastWatered = lastWatered;
    }

    public Duration getWateringInterval() {
        return wateringInterval;
    }

    public final void setWateringInterval(Duration wateringInterval) {
        this.wateringInterval = wateringInterval;
    }

    public Optional<byte[]> getPhoto() {
        return Optional.ofNullable(photo);
    }

    public final void setPhoto(byte[] photo) {
        if (Objects.nonNull(photo)) {
            this.photo = Arrays.copyOf(photo, photo.length);
        }
    }


    public boolean isNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(boolean notificationSent) {
        this.notificationSent = notificationSent;
    }
}
