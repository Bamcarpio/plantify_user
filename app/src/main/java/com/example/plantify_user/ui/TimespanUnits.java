package com.example.plantify_user.ui;

import java.util.Comparator;

import java.util.Comparator;

public enum TimespanUnits {
    YEARS(525_600),
    MONTHS(43_805),
    DAYS(1440),
    HOURS(60),
    MINUTES(1);

    static final Comparator<TimespanUnits> descendingOrder = Comparator
            .comparingLong(TimespanUnits::getMinutesInThis)
            .reversed();

    private final int minutesInThis;

    TimespanUnits(int numberOfMinutes) {
        minutesInThis = numberOfMinutes;
    }

    long fromMinutes(long minutes) {
        return minutes / minutesInThis;
    }

    long toMinutes(long scaledValue) {
        return scaledValue * minutesInThis;
    }

    int getMinutesInThis() {
        return minutesInThis;
    }
}

