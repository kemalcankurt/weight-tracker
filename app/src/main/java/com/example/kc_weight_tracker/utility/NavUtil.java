package com.example.kc_weight_tracker.utility;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.kc_weight_tracker.*;

public class NavUtil {

    /// reference: [...](https://developer.android.com/develop/ui/views/components/menus#java)
    public static void go(Context ctx, int id) {
        if (id == R.id.menu_logout) {
            // centralized logout
            SessionManager.clear(ctx);
            Toast.makeText(ctx, "Logged out", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(ctx, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ctx.startActivity(i);
            return;
        }

        // navigate to the appropriate activity
        Class<?> target = null;
        if (id == R.id.menu_home) {
            target = HomeActivity.class;
        } else if (id == R.id.menu_weight) {
            target = WeightTrackingActivity.class;
        } else if (id == R.id.menu_notify) {
            target = NotificationActivity.class;
        } else if (id == R.id.menu_settings) {
            target = SettingsActivity.class;
        }

        // navigate to the activity
        if (target != null && !ctx.getClass().equals(target)) {
            Intent i = new Intent(ctx, target);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ctx.startActivity(i);
        }
    }
}
