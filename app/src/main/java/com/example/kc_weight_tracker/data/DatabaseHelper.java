package com.example.kc_weight_tracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.kc_weight_tracker.data.DatabaseContract.*;

/**
 * Database helper class for managing SQLite database creation and version management.
 * Handles database schema creation and upgrades for the weight tracking application.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Constructor for DatabaseHelper
     * @param ctx Application context
     */
    public DatabaseHelper(Context ctx) {
        super(ctx, DatabaseContract.DB_NAME, null, DatabaseContract.DB_VERSION);
    }

    /**
     * Creates the database tables when the database is first created.
     * Sets up Users table for user information and Weights table for weight entries.
     * Users table stores: user ID, name, email, height
     * Weights table stores: weight ID, user ID, date, weight value
     * 
     * @param db SQLite database instance
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table to store user profile information
        db.execSQL(
                "CREATE TABLE " + Users.TABLE + " (" +
                        Users.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        Users.COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                        Users.COL_PASSWORD + " TEXT NOT NULL, " +
                        Users.COL_HEIGHT_INCHES + " REAL DEFAULT 70.0, " +
                        Users.COL_CREATED_AT + " INTEGER NOT NULL" +
                        ")"
        );

        // Weights table
        db.execSQL(
                "CREATE TABLE " + Weights.TABLE + " (" +
                        Weights.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        Weights.COL_USER_ID + " INTEGER NOT NULL, " +
                        Weights.COL_DATE + " TEXT NOT NULL, " +
                        Weights.COL_WEIGHT_LB + " REAL NOT NULL, " +
                        "FOREIGN KEY(" + Weights.COL_USER_ID + ") REFERENCES " +
                        Users.TABLE + "(" + Users.COL_ID + ") ON DELETE CASCADE" +
                        ")"
        );

        // Weight Goals table
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + WeightGoals.TABLE + " (" +
                        WeightGoals.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        WeightGoals.COL_USER_ID + " INTEGER NOT NULL UNIQUE, " +  // enforce 1 goal/user
                        WeightGoals.COL_TARGET_LB + " REAL NOT NULL, " +
                        WeightGoals.COL_TARGET_DATE + " TEXT NOT NULL, " +        // YYYY-MM-DD
                        WeightGoals.COL_CREATED_AT + " INTEGER NOT NULL, " +
                        "FOREIGN KEY(" + WeightGoals.COL_USER_ID + ") REFERENCES " +
                        Users.TABLE + "(" + Users.COL_ID + ") ON DELETE CASCADE)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 2) {
            // Add height column to existing users table
            db.execSQL("ALTER TABLE " + Users.TABLE + " ADD COLUMN " + Users.COL_HEIGHT_INCHES + " REAL DEFAULT 70.0");
        }
        
        // For simplicity, recreate tables for major changes
        if (oldV < 1) {
            db.execSQL("DROP TABLE IF EXISTS " + Weights.TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Users.TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + WeightGoals.TABLE);
            onCreate(db);
        }
    }
}