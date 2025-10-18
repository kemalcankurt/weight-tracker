package com.example.kc_weight_tracker;

import android.content.Context;
import android.content.Intent;

public class NavUtil {

    /// reference: [...](https://developer.android.com/develop/ui/views/components/menus#java)
    public static void go(Context ctx, int id) {
        Class<?> target = null;

        if (id == R.id.menu_login) {
            target = MainActivity.class;
        } else if (id == R.id.menu_weight) {
            target = WeightTrackingActivity.class;
        } else if (id == R.id.menu_notify) {
            target = NotificationActivity.class;
        }

        if (target != null && !ctx.getClass().equals(target)) {
            Intent i = new Intent(ctx, target);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ctx.startActivity(i);
        }
    }
}
