package com.example.kc_weight_tracker.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kc_weight_tracker.data.DatabaseContract;
import com.example.kc_weight_tracker.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * WeightsRepository is a class that manages the weight data.
 * It is used to get the weight history for a user, add a new weight entry,
 * update an existing weight entry, delete a weight entry, and get the latest
 * weight entry for a user.
 */
public class WeightsRepository {
    private final DatabaseHelper helper;

    public WeightsRepository(Context ctx) {
        this.helper = new DatabaseHelper(ctx.getApplicationContext());
    }

    /**
     * WeightDTO is a class that represents a weight entry.
     * It is used to store the id, user id, date, and weight.
     */
    public static final class WeightDTO {
        public final long id;
        public final long userId;
        public final String dateIso;
        public final float weightLb;

        /**
         * Constructor for WeightDTO
         * 
         * @param id       the id of the weight entry
         * @param userId   the id of the user
         * @param dateIso  the date of the weight entry
         * @param weightLb the weight of the weight entry
         */
        public WeightDTO(long id, long userId, String dateIso, float weightLb) {
            this.id = id;
            this.userId = userId;
            this.dateIso = dateIso;
            this.weightLb = weightLb;
        }
    }

    /**
     * Add a new weight entry
     * 
     * @param userId   the id of the user
     * @param dateIso  the date of the weight entry
     * @param weightLb the weight of the weight entry
     * @return the id of the weight entry if the addition was successful, -1
     *         otherwise
     */
    public long addWeight(long userId, String dateIso, float weightLb) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.Weights.COL_USER_ID, userId);
        cv.put(DatabaseContract.Weights.COL_DATE, dateIso);
        cv.put(DatabaseContract.Weights.COL_WEIGHT_LB, weightLb);
        return db.insert(DatabaseContract.Weights.TABLE, null, cv);
    }

    /**
     * Update an existing weight entry
     * 
     * @param id          the id of the weight entry
     * @param newWeightLb the new weight of the weight entry
     * @return the number of rows updated
     */
    public int updateWeight(long id, float newWeightLb) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.Weights.COL_WEIGHT_LB, newWeightLb);
        return db.update(
                DatabaseContract.Weights.TABLE,
                cv,
                DatabaseContract.Weights.COL_ID + "=?",
                new String[] { String.valueOf(id) });
    }

    /**
     * Delete a weight entry
     * 
     * @param id the id of the weight entry
     * @return the number of rows deleted
     */
    public int deleteWeight(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(
                DatabaseContract.Weights.TABLE,
                DatabaseContract.Weights.COL_ID + "=?",
                new String[] { String.valueOf(id) });
    }

    /**
     * Get the weight history for a user
     * 
     * @param userId the id of the user
     * @return the weight history for the user
     */
    public List<WeightDTO> getWeightHistory(long userId) {
        List<WeightDTO> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] cols = {
                DatabaseContract.Weights.COL_ID,
                DatabaseContract.Weights.COL_USER_ID,
                DatabaseContract.Weights.COL_DATE,
                DatabaseContract.Weights.COL_WEIGHT_LB
        };

        try (Cursor c = db.query(
                DatabaseContract.Weights.TABLE,
                cols,
                DatabaseContract.Weights.COL_USER_ID + "=?",
                new String[] { String.valueOf(userId) },
                null, null,
                DatabaseContract.Weights.COL_DATE + " DESC")) {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                long uid = c.getLong(1);
                String dateIso = c.getString(2);
                float weight = c.getFloat(3);
                list.add(new WeightDTO(id, uid, dateIso, weight));
            }
        }

        return list;
    }

    /**
     * Get the latest weight entry for a user
     * 
     * @param userId the id of the user
     * @return the latest weight entry for the user
     */
    public Float getLatestWeight(long userId) {
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] cols = { DatabaseContract.Weights.COL_WEIGHT_LB };

        try (Cursor c = db.query(
                DatabaseContract.Weights.TABLE,
                cols,
                DatabaseContract.Weights.COL_USER_ID + "=?",
                new String[] { String.valueOf(userId) },
                null, null,
                DatabaseContract.Weights.COL_DATE + " DESC",
                "1")) {
            if (c.moveToFirst()) {
                return c.getFloat(0);
            }
        }

        return null;
    }

    /**
     * Get the first weight entry for a user (earliest date)
     * Used to determine starting weight for progress calculation
     * 
     * @param userId the id of the user
     * @return the first weight entry for the user, or null if none exists
     */
    public Float getFirstWeight(long userId) {
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] cols = { DatabaseContract.Weights.COL_WEIGHT_LB };

        try (Cursor c = db.query(
                DatabaseContract.Weights.TABLE,
                cols,
                DatabaseContract.Weights.COL_USER_ID + "=?",
                new String[] { String.valueOf(userId) },
                null, null,
                DatabaseContract.Weights.COL_DATE + " ASC", // ASC for earliest first
                "1")) {
            if (c.moveToFirst()) {
                return c.getFloat(0);
            }
        }

        return null;
    }

    /**
     * Check if a weight entry exists for a specific date
     * 
     * @param userId  the id of the user
     * @param dateIso the date of the weight entry
     * @return true if a weight entry exists for the date, false otherwise
     */
    public boolean hasWeightEntry(long userId, String dateIso) {
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] cols = { DatabaseContract.Weights.COL_ID };

        try (Cursor c = db.query(
                DatabaseContract.Weights.TABLE,
                cols,
                DatabaseContract.Weights.COL_USER_ID + "=? AND " + DatabaseContract.Weights.COL_DATE + "=?",
                new String[] { String.valueOf(userId), dateIso },
                null, null, null,
                "1")) {
            return c.moveToFirst();
        }
    }
}
