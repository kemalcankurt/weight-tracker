package com.example.kc_weight_tracker.utility;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager is a class that manages the session of the user.
 * It is a singleton class that is used to manage the session of the user.
 */
public final class SessionManager {
    private SessionManager() {
    }

    /// get the shared preferences for the session
    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    /// get the user id from the shared preferences
    public static long userId(Context ctx) {
        return prefs(ctx).getLong("user_id", -1L);
    }

    /// get the username from the shared preferences
    public static String username(Context ctx) {
        return prefs(ctx).getString("username", null);
    }

    /// check if the user is logged in
    public static boolean isLoggedIn(Context ctx) {
        return userId(ctx) > 0;
    }

    /// clear the session
    public static void clear(Context ctx) {
        prefs(ctx).edit().clear().apply();
    }
}