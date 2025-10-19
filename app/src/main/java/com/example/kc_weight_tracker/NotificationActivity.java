package com.example.kc_weight_tracker;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.kc_weight_tracker.utility.NavUtil;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private TextView tvNotificationHistory;
    private List<String> notificationLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Toolbar
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notification History");
        }

        tvNotificationHistory = findViewById(R.id.tvNotificationHistory);

        // Initialize notification log
        notificationLog = new ArrayList<>();
        loadNotificationHistory();
        updateNotificationDisplay();
    }

    private void loadNotificationHistory() {
        // In future, notifications would load from a database
        // However, such implementation is out of scope for now
        // Yet, for demo purposes, we'll add some sample notifications
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        notificationLog.add(sdf.format(new Date()) + " - Daily weight reminder sent");
        notificationLog.add(
                sdf.format(new Date(System.currentTimeMillis() - 86400000)) + " - Goal progress notification sent");
        notificationLog.add(
                sdf.format(new Date(System.currentTimeMillis() - 172800000)) + " - Weekly summary notification sent");
    }

    private void updateNotificationDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Recent Notifications:\n\n");

        for (String notification : notificationLog) {
            sb.append("• ").append(notification).append("\n");
        }

        if (notificationLog.isEmpty()) {
            sb.append("No notifications sent yet.\n\n");
            sb.append("Enable notifications in Settings to start receiving reminders!");
        }

        tvNotificationHistory.setText(sb.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        NavUtil.go(this, item.getItemId());
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
