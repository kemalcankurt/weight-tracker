package com.example.kc_weight_tracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class NotificationActivity extends AppCompatActivity {

    private SwitchMaterial swSms, swPush;

    private final ActivityResultLauncher<String> requestSmsPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                swSms.setChecked(granted);
                Toast.makeText(this, "SMS: " + (granted ? "granted" : "denied"), Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<String> requestPushPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                swPush.setChecked(granted);
                Toast.makeText(this, "Push: " + (granted ? "granted" : "denied"), Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Toolbar
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Notifications");

        swSms = findViewById(R.id.swSms);
        swPush = findViewById(R.id.swPush);

        // Initialize toggle states
        swSms.setChecked(hasSmsPermission());
        swPush.setChecked(hasPushPermission());

        // Toggle listeners
        // reference: https://developer.android.com/develop/ui/views/components/togglebutton
        swSms.setOnCheckedChangeListener((b, checked) -> {
            if (checked && !hasSmsPermission()) {
                requestSmsPermission.launch(Manifest.permission.SEND_SMS);
            }
        });

        swPush.setOnCheckedChangeListener((b, checked) -> {
            if (checked && !hasPushPermission()) {
                requestPushPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        });
    }

    /// reference: [...](https://developer.android.com/develop/ui/views/components/menus#java)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavUtil.go(this, item.getItemId());
        return true;
    }


    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasPushPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }
}
