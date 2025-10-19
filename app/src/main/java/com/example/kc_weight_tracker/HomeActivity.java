package com.example.kc_weight_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kc_weight_tracker.repository.WeightGoalRepository;
import com.example.kc_weight_tracker.repository.WeightsRepository;
import com.example.kc_weight_tracker.utility.NavUtil;
import com.example.kc_weight_tracker.utility.SessionManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

/**
 * HomeActivity is the main activity of the app.
 * It displays the home screen of the app.
 */
public class HomeActivity extends AppCompatActivity {
    private TextView tvWelcome, tvToday, tvGoalTitle, tvGoalWeight, tvGoalSubtitle;
    private TextView tvCurrentWeight, tvStreak;
    private MaterialCardView cardLogWeight, cardNotificationHistory, cardSettings, cardSetGoal;
    private CircularProgressView circularProgress;
    private FloatingActionButton fabLogWeight;

    /**
     * Called when the activity is created
     * 
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialToolbar bar = findViewById(R.id.topAppBar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Weight Tracker");
        }

        initializeViews();
        setupClickListeners();
        updateUI();
    }

    /**
     * Initialize the views
     */
    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvToday = findViewById(R.id.tvToday);
        tvGoalTitle = findViewById(R.id.tvGoalTitle);
        tvGoalWeight = findViewById(R.id.tvGoalWeight);
        tvGoalSubtitle = findViewById(R.id.tvGoalSubtitle);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvStreak = findViewById(R.id.tvStreak);

        cardLogWeight = findViewById(R.id.cardLogWeight);
        cardNotificationHistory = findViewById(R.id.cardNotificationHistory);
        cardSettings = findViewById(R.id.cardSettings);
        cardSetGoal = findViewById(R.id.cardSetGoal);

        circularProgress = findViewById(R.id.circularProgress);
        fabLogWeight = findViewById(R.id.fabLogWeight);
    }

    /**
     * Set up the click listeners
     */
    private void setupClickListeners() {
        // Card click listeners
        cardLogWeight.setOnClickListener(v -> startActivity(new Intent(this, WeightTrackingActivity.class)));
        cardNotificationHistory.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        cardSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        cardSetGoal.setOnClickListener(v -> {
            // Show bottom sheet dialog for setting goal
            // Refresh UI when goal is set
            SetGoalBottomSheetDialog dialog = SetGoalBottomSheetDialog.newInstance(this::updateUI);
            dialog.show(getSupportFragmentManager(), "SetGoalDialog");
        });

        // Floating Action Button
        fabLogWeight.setOnClickListener(v -> startActivity(new Intent(this, WeightTrackingActivity.class)));
    }

    /**
     * Update the UI after a change
     */
    private void updateUI() {
        // Update the welcome message, date, goal progress, and quick stats
        updateWelcomeMessage(); // Update the welcome message
        updateDate(); // Update the date
        updateGoalProgress(); // Update the goal progress
        updateQuickStats(); // Update the quick stats
    }

    // Update the welcome message
    private void updateWelcomeMessage() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = "Good morning!";
        } else if (hour < 17) {
            greeting = "Good afternoon!";
        } else {
            greeting = "Good evening!";
        }
        tvWelcome.setText(greeting);
    }

    // Update the date
    private void updateDate() {
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        String month = today.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        int day = today.getDayOfMonth();

        tvToday.setText(String.format("%s, %s %d", dayOfWeek, month, day));
    }

    // Update the goal progress
    private void updateGoalProgress() {
        long uid = SessionManager.userId(this);
        WeightGoalRepository goalRepo = new WeightGoalRepository(this);
        WeightsRepository weightsRepo = new WeightsRepository(this);

        // Get the current goal
        WeightGoalRepository.WeightGoalDTO goal = goalRepo.getCurrentGoal(uid);

        if (goal == null) {
            tvGoalTitle.setText("Weight Goal");
            tvGoalWeight.setText("Not Set");
            tvGoalSubtitle.setText("Tap 'Set Goal' to create one");
            circularProgress.setProgress(0);
            circularProgress.setCenterText("0%");
        } else {
            tvGoalTitle.setText("Weight Goal");
            tvGoalWeight.setText(String.format("%.1f lb", goal.targetLb));

            // Format target date nicely
            try {
                LocalDate targetDate = LocalDate.parse(goal.targetDateIso);
                String month = targetDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
                int day = targetDate.getDayOfMonth();
                int year = targetDate.getYear();
                tvGoalSubtitle.setText(String.format("Target: %s %d, %d", month, day, year));
            } catch (Exception e) {
                tvGoalSubtitle.setText("Target: " + goal.targetDateIso);
            }

            /*
             * Basic Goal Progress Calculation:
             * Progress = (Total Difference - Current Difference) / Total Difference × 100
             * 
             * Where:
             * - Total Difference = |Starting Weight - Goal Weight|
             * - Current Difference = |Current Weight - Goal Weight|
             * 
             * Examples:
             * Weight Loss: Start 200lbs, Goal 180lbs, Current 190lbs
             * Total Diff = |200-180| = 20, Current Diff = |190-180| = 10
             * Progress = (20-10)/20 × 100 = 50%
             * 
             * Weight Gain: Start 150lbs, Goal 170lbs, Current 165lbs
             * Total Diff = |150-170| = 20, Current Diff = |165-170| = 5
             * Progress = (20-5)/20 × 100 = 75%
             */
            Float currentWeight = weightsRepo.getLatestWeight(uid);
            if (currentWeight != null) {
                // Get the first weight entry as starting weight
                Float startingWeight = weightsRepo.getFirstWeight(uid);

                if (startingWeight != null) {
                    // Calculate difference between current weight and goal
                    float weightDifference = Math.abs(currentWeight - goal.targetLb);
                    float totalDifference = Math.abs(startingWeight - goal.targetLb);

                    if (totalDifference > 0) {
                        // Calculate progress based on how close they are to the goal
                        float progress = Math.max(0,
                                Math.min(100, ((totalDifference - weightDifference) / totalDifference) * 100));

                        circularProgress.setProgress(progress);

                        if (weightDifference <= 1.0f) {
                            // Very close to goal (within 1 lb)
                            circularProgress.setCenterText("Almost There!");
                        } else if (weightDifference <= 5.0f) {
                            // Close to goal (within 5 lbs)
                            circularProgress.setCenterText(String.format("%.1f lbs to go", weightDifference));
                        } else {
                            // Show percentage progress
                            circularProgress.setCenterText(String.format("%.0f%%", progress));
                        }
                    } else {
                        // Goal already reached
                        circularProgress.setProgress(100);
                        circularProgress.setCenterText("Goal Reached!");
                    }
                } else {
                    // No starting weight available
                    circularProgress.setProgress(0);
                    circularProgress.setCenterText("0%");
                }
            } else {
                circularProgress.setProgress(0);
                circularProgress.setCenterText("0%");
            }
        }
    }

    /**
     * Update the quick stats (current weight and streak)
     */
    private void updateQuickStats() {
        long uid = SessionManager.userId(this);
        WeightsRepository weightsRepo = new WeightsRepository(this);

        Float currentWeight = weightsRepo.getLatestWeight(uid);
        if (currentWeight != null) {
            tvCurrentWeight.setText(String.format("%.1f", currentWeight));
        } else {
            tvCurrentWeight.setText("--");
        }

        int streak = calculateStreak(uid);
        tvStreak.setText(String.valueOf(streak));
    }

    /**
     * Calculate the streak
     * 
     * @param uid the id of the user
     * @return the streak
     */
    private int calculateStreak(long uid) {
        /**
         * Calculate consecutive days of weight logging (streak)
         * 
         * Algorithm:
         * 1. Start from today and work backwards
         * 2. Check each day for a weight entry
         * 3. If entry exists, increment streak counter
         * 4. If no entry found, break the streak
         * 5. Return total consecutive days
         * 
         */
        WeightsRepository weightsRepo = new WeightsRepository(this);
        LocalDate today = LocalDate.now();
        int streak = 0;

        for (int i = 0; i < 30; i++) { // Check last 30 days
            LocalDate checkDate = today.minusDays(i);
            if (weightsRepo.hasWeightEntry(uid, checkDate.toString())) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * Called when the activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    /**
     * Create the options menu
     * 
     * @param menu the menu
     * @return true if the menu was created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    /**
     * Handle the options menu item selection
     * 
     * @param item the menu item
     * @return true if the item was selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavUtil.go(this, item.getItemId());
        return true;
    }
}
