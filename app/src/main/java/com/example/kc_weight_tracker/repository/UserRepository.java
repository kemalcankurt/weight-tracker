package com.example.kc_weight_tracker.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static com.example.kc_weight_tracker.data.DatabaseContract.Users;

import com.example.kc_weight_tracker.data.DatabaseHelper;

/**
 * UserRepository is a class that manages the user data.
 * It is used to create a new user, check if a user exists, authenticate a user,
 * and get the user's height.
 * It is also used to update the user's height.
 */
public class UserRepository {
    private final DatabaseHelper helper;

    /**
     * Constructor for UserRepository
     * 
     * @param ctx the context of the application
     */
    public UserRepository(Context ctx) {
        this.helper = new DatabaseHelper(ctx.getApplicationContext());
    }

    /**
     * Create a new user
     * 
     * @param username    the username of the user
     * @param rawPassword the raw password of the user
     * @return the user id if the creation was successful, -1 otherwise
     */
    public long createUser(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank())
            return -1;

        SQLiteDatabase db = helper.getWritableDatabase();

        // Uniqueness check
        if (userExists(username))
            return -1;

        ContentValues cv = new ContentValues();
        cv.put(Users.COL_USERNAME, username.trim());
        cv.put(Users.COL_PASSWORD, rawPassword); // DEMO ONLY — swap with hashed value later
        cv.putNull(Users.COL_HEIGHT_INCHES);
        cv.put(Users.COL_CREATED_AT, System.currentTimeMillis());
        return db.insert(Users.TABLE, null, cv);
    }

    /**
     * Check if the user exists
     * 
     * @param username the username of the user
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(String username) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(
                Users.TABLE,
                new String[] { Users.COL_ID },
                Users.COL_USERNAME + "=?",
                new String[] { username },
                null, null, null)) {
            return c.moveToFirst();
        }
    }

    /**
     * Authenticate the user with the username and password
     * 
     * @param username    the username of the user
     * @param rawPassword the raw password of the user
     * @return the user id if the authentication was successful, -1 otherwise
     */
    public long authenticate(String username, String rawPassword) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(
                Users.TABLE,
                new String[] { Users.COL_ID, Users.COL_PASSWORD },
                Users.COL_USERNAME + "=?",
                new String[] { username },
                null, null, null)) {
            if (!c.moveToFirst())
                return -1;
            String stored = c.getString(c.getColumnIndexOrThrow(Users.COL_PASSWORD));
            if (stored.equals(rawPassword)) { // DEMO ONLY — replace with hash compare later
                return c.getLong(c.getColumnIndexOrThrow(Users.COL_ID));
            }
            return -1;
        }
    }

    /**
     * Get the user's height in inches
     * 
     * @param userId the id of the user
     * @return the user's height in inches
     */
    public Double getUserHeight(long userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(
                Users.TABLE,
                new String[] { Users.COL_HEIGHT_INCHES },
                Users.COL_ID + "=?",
                new String[] { String.valueOf(userId) },
                null, null, null)) {
            if (c.moveToFirst()) {
                int heightIndex = c.getColumnIndex(Users.COL_HEIGHT_INCHES);
                if (c.isNull(heightIndex)) {
                    return null; // No height set
                }
                return c.getDouble(heightIndex);
            }
        }
        return null; // User not found or no height set
    }

    /**
     * Update the user's height in inches
     * 
     * @param userId       the id of the user
     * @param heightInches the height in inches
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUserHeight(long userId, double heightInches) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(Users.COL_HEIGHT_INCHES, heightInches);

        int rowsAffected = db.update(
                Users.TABLE,
                cv,
                Users.COL_ID + "=?",
                new String[] { String.valueOf(userId) });

        return rowsAffected > 0;
    }
}
