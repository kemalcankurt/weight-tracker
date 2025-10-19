package com.example.kc_weight_tracker.data;

/**
 * DatabaseContract is a class that contains the contract for the database.
 * It is used to define the tables and columns for the database.
 */
public final class DatabaseContract {
    private DatabaseContract() {
    }

    /// the name of the database
    public static final String DB_NAME = "kc_weight_tracker.db";
    public static final int DB_VERSION = 2;

    /// the users table
    public static final class Users {
        public static final String TABLE = "users";
        public static final String COL_ID = "_id";
        public static final String COL_USERNAME = "username";
        public static final String COL_PASSWORD = "password";
        public static final String COL_HEIGHT_INCHES = "height_inches";
        public static final String COL_CREATED_AT = "created_at";
    }

    /// the weights table
    public static final class Weights {
        public static final String TABLE = "weights";
        public static final String COL_ID = "_id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_DATE = "date_iso";
        public static final String COL_WEIGHT_LB = "weight_lb";
    }

    /// the weight goals table
    public static final class WeightGoals {
        public static final String TABLE = "goals";
        public static final String COL_ID = "_id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_TARGET_LB = "target_lb";
        public static final String COL_TARGET_DATE = "target_date_iso";
        public static final String COL_CREATED_AT = "created_at";
    }
}
