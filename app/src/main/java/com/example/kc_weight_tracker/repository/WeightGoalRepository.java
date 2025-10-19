package com.example.kc_weight_tracker.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.kc_weight_tracker.data.DatabaseContract;
import com.example.kc_weight_tracker.data.DatabaseHelper;

import static com.example.kc_weight_tracker.data.DatabaseContract.WeightGoals;

/**
 * WeightGoalRepository is a class that manages the weight goal data.
 * It is used to get the current goal for a user, insert or replace a goal, and
 * clear a goal.
 */
public class WeightGoalRepository {
    private final DatabaseHelper helper;

    /**
     * Constructor for WeightGoalRepository
     * 
     * @param ctx the context of the application
     */
    public WeightGoalRepository(Context ctx) {
        this.helper = new DatabaseHelper(ctx.getApplicationContext());
    }

    /**
     * WeightGoalDTO is a class that represents a weight goal.
     * It is used to store the user id, target weight, target date, and created at
     * epoch milliseconds.
     */
    public static final class WeightGoalDTO {
        public final long userId;
        public final float targetLb;
        public final String targetDateIso;
        public final long createdAtEpochMs;

        /**
         * Constructor for WeightGoalDTO
         * 
         * @param userId           the id of the user
         * @param targetLb         the target weight
         * @param targetDateIso    the target date
         * @param createdAtEpochMs the created at epoch milliseconds
         */
        public WeightGoalDTO(long userId, float targetLb, String targetDateIso, long createdAtEpochMs) {
            this.userId = userId;
            this.targetLb = targetLb;
            this.targetDateIso = targetDateIso;
            this.createdAtEpochMs = createdAtEpochMs;
        }
    }

    /**
     * Insert or replace the user's single goal.
     * 
     * @param userId           the id of the user
     * @param targetLb         the target weight
     * @param targetDateIso    the target date
     * @return the id of the goal if the insert or replace was successful, -1 otherwise
     */
    public long upsertGoal(long userId, float targetLb, String targetDateIso) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(WeightGoals.COL_USER_ID, userId);
        cv.put(WeightGoals.COL_TARGET_LB, targetLb);
        cv.put(WeightGoals.COL_TARGET_DATE, targetDateIso);
        cv.put(WeightGoals.COL_CREATED_AT, System.currentTimeMillis());

        return db.insertWithOnConflict(
                DatabaseContract.WeightGoals.TABLE,
                null,
                cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Get the current goal for the user
     * 
     * @param userId the id of the user
     * @return the current goal for the user, or null if none
     */
    @Nullable
    public WeightGoalDTO getCurrentGoal(long userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(
                WeightGoals.TABLE,
                new String[] { WeightGoals.COL_USER_ID, WeightGoals.COL_TARGET_LB, WeightGoals.COL_TARGET_DATE,
                        WeightGoals.COL_CREATED_AT },
                WeightGoals.COL_USER_ID + "=?",
                new String[] { String.valueOf(userId) },
                null, null, null, "1")) {
            if (!c.moveToFirst())
                return null;
            return new WeightGoalDTO(
                    c.getLong(c.getColumnIndexOrThrow(WeightGoals.COL_USER_ID)),
                    c.getFloat(c.getColumnIndexOrThrow(WeightGoals.COL_TARGET_LB)),
                    c.getString(c.getColumnIndexOrThrow(WeightGoals.COL_TARGET_DATE)),
                    c.getLong(c.getColumnIndexOrThrow(WeightGoals.COL_CREATED_AT)));
        }
    }

    /**
     * Clear the user's goal
     * 
     * @param userId the id of the user
     * @return the number of rows deleted
     */
    public int clear(long userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(WeightGoals.TABLE, WeightGoals.COL_USER_ID + "=?", new String[] { String.valueOf(userId) });
    }
}
