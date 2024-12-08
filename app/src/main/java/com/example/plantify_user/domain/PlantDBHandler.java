package com.example.plantify_user.domain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PlantDBHandler extends SQLiteOpenHelper {

    private static class TableColumn {
        public final int index;
        public final String name;

        public TableColumn(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME;
    public static final String TABLE_PLANTS;

    public static final TableColumn COLUMN_ID;
    public static final TableColumn COLUMN_NAME;
    public static final TableColumn COLUMN_BIRTHDAY;
    public static final TableColumn COLUMN_LAST_WATERED;
    public static final TableColumn COLUMN_WATERING_INTERVAL;
    public static final TableColumn COLUMN_PHOTO;
    public static final HashMap<String, TableColumn> columns = new HashMap<>();

    static {
        DATABASE_NAME = "plantDB";
        TABLE_PLANTS = "Plant";

        COLUMN_ID = new TableColumn(0, "_id");
        COLUMN_NAME = new TableColumn(1, "name");
        COLUMN_BIRTHDAY = new TableColumn(2, "birthday");
        COLUMN_LAST_WATERED = new TableColumn(3, "last_watered");
        COLUMN_WATERING_INTERVAL = new TableColumn(4, "watering_interval");
        COLUMN_PHOTO = new TableColumn(5, "photo");

        columns.put(COLUMN_ID.name, COLUMN_ID);
        columns.put(COLUMN_NAME.name, COLUMN_NAME);
        columns.put(COLUMN_BIRTHDAY.name, COLUMN_BIRTHDAY);
        columns.put(COLUMN_LAST_WATERED.name, COLUMN_LAST_WATERED);
        columns.put(COLUMN_WATERING_INTERVAL.name, COLUMN_WATERING_INTERVAL);
        columns.put(COLUMN_PHOTO.name, COLUMN_PHOTO);
    }

    public PlantDBHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // create the Plant table
        final String createPlantTableQuery =
                "CREATE TABLE " + TABLE_PLANTS + " (\n" +
                        COLUMN_ID.name + " INTEGER  NOT NULL  ,\n" +
                        COLUMN_NAME.name + " TEXT  NOT NULL  ,\n" +
                        COLUMN_BIRTHDAY.name + " TEXT    ,\n" +
                        COLUMN_LAST_WATERED.name + " TEXT  NOT NULL  ,\n" +
                        COLUMN_WATERING_INTERVAL.name + " INTEGER  NOT NULL  ,\n" +
                        COLUMN_PHOTO.name + " BLOB      ,\n" +
                        "PRIMARY KEY(" + COLUMN_ID.name + "));";

        db.execSQL(createPlantTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANTS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public void addPlant(Plant plant) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues contentValues = new ContentValues();

            // Add the plant's values (ID is auto incremented)
            putNonIds(plant, contentValues);

            db.insert(TABLE_PLANTS, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public List<Plant> getAllPlants() {
        String query = "SELECT * " +
                "FROM " + TABLE_PLANTS;

        return getPlants(query);
    }


    public Optional<Plant> getPlantById(long id) {
        String query = "SELECT *\n" +
                "FROM " + TABLE_PLANTS + "\n" +
                "WHERE _id = " + id + "\n";

        List<Plant> result = getPlants(query);
        if (!result.isEmpty()) {
            return Optional.of(result.get(0));
        }
        return Optional.empty();
    }


    public void updatePlant(Plant plant) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues contentValues = new ContentValues();

            // Add the plant's values (ID is auto incremented)
            putNonIds(plant, contentValues);

            db.update(TABLE_PLANTS, contentValues,
                    COLUMN_ID.name + " = ?", new String[]{Long.toString(plant.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean removePlant(long id) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            return db.delete(TABLE_PLANTS, COLUMN_ID.name + " = ?",
                    new String[]{Long.toString(id)}) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private void putNonIds(Plant plant, ContentValues contentValues) {
        contentValues.put(COLUMN_NAME.name, Objects.requireNonNull(plant.getName()));
        contentValues.put(COLUMN_LAST_WATERED.name, plant.getLastWatered().toString());
        contentValues.put(COLUMN_WATERING_INTERVAL.name, plant.getWateringInterval().toString());
        contentValues.put(COLUMN_BIRTHDAY.name, plant.getBirthday().map(Object::toString).orElse(null));
        contentValues.put(COLUMN_PHOTO.name, plant.getPhoto().orElse(null));
    }


    private List<Plant> getPlants(String query) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.rawQuery(query, null)) {
                if (!cursor.moveToFirst()) {
                    return Collections.emptyList();
                }
                List<Plant> result = new ArrayList<>(cursor.getCount() + 1);
                for (; !cursor.isAfterLast(); cursor.moveToNext()) {
                    // create the new object
                    Plant current = new Plant();
                    // id
                    current.setId(cursor.getLong(COLUMN_ID.index));
                    // name
                    current.setName(cursor.getString(COLUMN_NAME.index));
                    // birthday
                    current.setBirthday(
                            Optional.ofNullable(cursor.getString(COLUMN_BIRTHDAY.index))
                                    .map(LocalDateTime::parse)
                                    .orElse(null)
                    );
                    // last watered
                    current.setLastWatered(
                            LocalDateTime.parse(cursor.getString(COLUMN_LAST_WATERED.index))
                    );
                    // watering interval
                    current.setWateringInterval(
                            Duration.parse(cursor.getString(COLUMN_WATERING_INTERVAL.index))
                    );
                    // photo
                    current.setPhoto(
                            Optional.ofNullable(cursor.getBlob(COLUMN_PHOTO.index))
                                    .orElse(null)
                    );
                    result.add(current); // and add it to the result
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
