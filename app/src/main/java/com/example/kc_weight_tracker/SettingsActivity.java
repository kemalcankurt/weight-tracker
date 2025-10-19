package com.example.kc_weight_tracker;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.kc_weight_tracker.repository.UserRepository;
import com.example.kc_weight_tracker.utility.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * SettingsActivity is the activity that allows the user to view and edit their
 * settings.
 */
public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private NumberPicker npHeight;
    private TextView tvHeightDisplay;
    private MaterialButton btnSaveHeight;
    private SwitchMaterial switchDailyReminder, switchGoalReminders;
    private TextView tvReminderTime;
    private MaterialButton btnSetTime;
    private MaterialButton btnChangeUnits, btnExportData;

    // Data
    private UserRepository userRepository;
    private long userId;
    private double currentHeight;
    private int reminderHour = 9;
    private int reminderMinute = 0;

    // SharedPreferences for notification settings
    private SharedPreferences notificationPrefs;

    // Track if user manually toggled switches to avoid duplicate messages
    private boolean userToggledSms = false;
    private boolean userToggledPush = false;

    // Permission request launchers
    private final ActivityResultLauncher<String> requestSmsPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                // Temporarily disable listener to avoid duplicate messages
                switchDailyReminder.setOnCheckedChangeListener(null);
                switchDailyReminder.setChecked(granted);

                if (userToggledSms) {
                    if (granted) {
                        Toast.makeText(this, "SMS permission granted! You'll receive weight reminders via SMS.",
                                Toast.LENGTH_LONG).show();
                        saveNotificationPreferences(); // Save preference
                    } else {
                        Toast.makeText(this, "SMS permission denied. You can enable it later in Settings.",
                                Toast.LENGTH_LONG).show();
                    }
                    userToggledSms = false; // Reset flag
                }

                // Re-enable listener
                setupNotificationListeners();
            });

    // Request push permission launcher
    private final ActivityResultLauncher<String> requestPushPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                // Temporarily disable listener to avoid duplicate messages
                switchGoalReminders.setOnCheckedChangeListener(null);
                switchGoalReminders.setChecked(granted);

                if (userToggledPush) {
                    if (granted) {
                        Toast.makeText(this, "Push notification permission granted! You'll receive daily reminders.",
                                Toast.LENGTH_LONG).show();
                        saveNotificationPreferences(); // Save preference
                    } else {
                        Toast.makeText(this,
                                "Push notification permission denied. You can enable it later in Settings.",
                                Toast.LENGTH_LONG).show();
                    }
                    userToggledPush = false; // Reset flag
                }

                // Re-enable listener
                setupNotificationListeners();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setupToolbar();
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.topAppBar);
        npHeight = findViewById(R.id.npHeight);
        tvHeightDisplay = findViewById(R.id.tvHeightDisplay);
        btnSaveHeight = findViewById(R.id.btnSaveHeight);
        switchDailyReminder = findViewById(R.id.switchDailyReminder);
        switchGoalReminders = findViewById(R.id.switchGoalReminders);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        btnSetTime = findViewById(R.id.btnSetTime);
        btnChangeUnits = findViewById(R.id.btnChangeUnits);
        btnExportData = findViewById(R.id.btnExportData);

        userRepository = new UserRepository(this);
        userId = SessionManager.userId(this);
        notificationPrefs = getSharedPreferences("notification_settings", MODE_PRIVATE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }

    // * Load the user data */
    private void loadUserData() {
        // Load current height
        Double heightValue = userRepository.getUserHeight(userId);
        currentHeight = heightValue != null ? heightValue : 70.0; // Default to 70 inches if not set

        // Setup height picker
        npHeight.setMinValue(48); // 4 feet
        npHeight.setMaxValue(84); // 7 feet
        npHeight.setValue((int) currentHeight);

        // Update height display
        updateHeightDisplay();

        // Load saved notification preferences
        loadNotificationPreferences();

        // Update reminder time display
        updateReminderTimeDisplay();
    }

    // * Setup the click listeners */
    private void setupClickListeners() {
        // Height save button
        btnSaveHeight.setOnClickListener(v -> saveHeight());

        // Height picker change listener
        npHeight.setOnValueChangedListener((picker, oldVal, newVal) -> {
            updateHeightDisplay();
        });

        // Time picker
        btnSetTime.setOnClickListener(v -> showTimePicker());

        // Setup notification listeners
        setupNotificationListeners();

        // Units change
        btnChangeUnits.setOnClickListener(v -> {
            // TODO: Implement units change
            Toast.makeText(this, "Units change coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Data export
        btnExportData.setOnClickListener(v -> {
            // TODO: Implement data export
            Toast.makeText(this, "Data export coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    // * Save the height */
    private void saveHeight() {
        double newHeight = npHeight.getValue();

        if (userRepository.updateUserHeight(userId, newHeight)) {
            currentHeight = newHeight;
            updateHeightDisplay();
            Toast.makeText(this, "Height updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update height", Toast.LENGTH_SHORT).show();
        }
    }

    // * Update the height display */
    private void updateHeightDisplay() {
        int heightInches = npHeight.getValue();
        int feet = heightInches / 12;
        int inches = heightInches % 12;

        String displayText;
        if (inches == 0) {
            displayText = String.format("Current: %d inches (%d'0\")", heightInches, feet);
        } else {
            displayText = String.format("Current: %d inches (%d'%d\")", heightInches, feet, inches);
        }

        tvHeightDisplay.setText(displayText);
    }

    // * Show the time picker */
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    reminderHour = hourOfDay;
                    reminderMinute = minute;
                    updateReminderTimeDisplay();
                    saveNotificationPreferences(); // Save reminder time
                    Toast.makeText(this, "Reminder time updated!", Toast.LENGTH_SHORT).show();
                },
                reminderHour,
                reminderMinute,
                false);

        timePickerDialog.show();
    }

    // * Update the reminder time display */
    private void updateReminderTimeDisplay() {
        String timeString = String.format("%d:%02d %s",
                reminderHour == 0 ? 12 : (reminderHour > 12 ? reminderHour - 12 : reminderHour),
                reminderMinute,
                reminderHour < 12 ? "AM" : "PM");
        tvReminderTime.setText(timeString);
    }

    // * Handle the options menu item selection */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // * On back pressed */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // * Load the notification preferences */
    private void loadNotificationPreferences() {
        // Load saved reminder time
        reminderHour = notificationPrefs.getInt("reminder_hour", 9);
        reminderMinute = notificationPrefs.getInt("reminder_minute", 0);

        // Load saved notification preferences (only if user has permission)
        boolean smsEnabled = notificationPrefs.getBoolean("sms_enabled", false) && hasSmsPermission();
        boolean pushEnabled = notificationPrefs.getBoolean("push_enabled", false) && hasPushPermission();

        // Set switches based on saved preferences and current permissions
        switchDailyReminder.setChecked(smsEnabled);
        switchGoalReminders.setChecked(pushEnabled);
    }

    private void saveNotificationPreferences() {
        notificationPrefs.edit()
                .putBoolean("sms_enabled", switchDailyReminder.isChecked())
                .putBoolean("push_enabled", switchGoalReminders.isChecked())
                .putInt("reminder_hour", reminderHour)
                .putInt("reminder_minute", reminderMinute)
                .apply();
    }

    // * Setup the notification listeners */
    private void setupNotificationListeners() {
        // Notification switches with permission handling
        switchDailyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !hasSmsPermission()) {
                userToggledSms = true;
                requestSmsPermission.launch(Manifest.permission.SEND_SMS);
            } else if (isChecked && hasSmsPermission()) {
                Toast.makeText(this, "SMS reminders enabled", Toast.LENGTH_SHORT).show();
                saveNotificationPreferences(); // Save preference
            } else if (!isChecked) {
                Toast.makeText(this, "SMS reminders disabled", Toast.LENGTH_SHORT).show();
                saveNotificationPreferences(); // Save preference
            }
        });

        switchGoalReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !hasPushPermission()) {
                userToggledPush = true;
                requestPushPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else if (isChecked && hasPushPermission()) {
                Toast.makeText(this, "Push notifications enabled", Toast.LENGTH_SHORT).show();
                saveNotificationPreferences(); // Save preference
            } else if (!isChecked) {
                Toast.makeText(this, "Push notifications disabled", Toast.LENGTH_SHORT).show();
                saveNotificationPreferences(); // Save preference
            }
        });
    }

    // * Check if the SMS permission is granted */
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    // * Check if the push permission is granted */
    private boolean hasPushPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }
}
