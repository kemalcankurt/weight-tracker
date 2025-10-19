package com.example.kc_weight_tracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kc_weight_tracker.repository.WeightsRepository;
import com.example.kc_weight_tracker.utility.NavUtil;
import com.example.kc_weight_tracker.utility.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * WeightTrackingActivity is the activity that allows the user to track their
 * weight history.
 */
public class WeightTrackingActivity extends AppCompatActivity {

    private TextInputEditText etDate, etWeight;
    private TextInputLayout tilWeight;
    private MaterialButton btnAdd;
    private MaterialButton btnWeightPlus1, btnWeightPlus5, btnWeightMinus1, btnWeightMinus5;
    private RecyclerView rvGrid;

    private WeightsRepository repo;
    private WeightsAdapter adapter;
    private long userId;

    /**
     * Creates the options menu
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
     * Handles the options menu item selection
     * 
     * @param item the menu item
     * @return true if the item was selected
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        NavUtil.go(this, item.getItemId());
        return true;
    }

    /**
     * Called when the activity is created
     * 
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_tracking);

        repo = new WeightsRepository(this);
        userId = SessionManager.userId(this);

        MaterialToolbar bar = findViewById(R.id.topAppBar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Weight History");
        }

        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        tilWeight = findViewById(R.id.tilWeight);
        btnAdd = findViewById(R.id.btnAdd);
        btnWeightPlus1 = findViewById(R.id.btnWeightPlus1);
        btnWeightPlus5 = findViewById(R.id.btnWeightPlus5);
        btnWeightMinus1 = findViewById(R.id.btnWeightMinus1);
        btnWeightMinus5 = findViewById(R.id.btnWeightMinus5);
        rvGrid = findViewById(R.id.rvGrid);

        // RecyclerView
        rvGrid.setLayoutManager(new LinearLayoutManager(this));
        List<WeightsRepository.WeightDTO> weightHistory = repo.getWeightHistory(userId);
        adapter = new WeightsAdapter(
                weightHistory,
                id -> { // onDelete
                    repo.deleteWeight(id);
                    refresh();
                });
        rvGrid.setAdapter(adapter);

        // Date picker
        TextInputLayout tilDate = findViewById(R.id.tilDate);
        tilDate.setEndIconOnClickListener(v -> showDatePicker());

        // Setup date hint
        setupDateHint();

        // Setup weight input
        setupWeightInput(weightHistory);

        // Quick adjust buttons
        btnWeightPlus1.setOnClickListener(v -> adjustWeight(1.0f));
        btnWeightPlus5.setOnClickListener(v -> adjustWeight(5.0f));
        btnWeightMinus1.setOnClickListener(v -> adjustWeight(-1.0f));
        btnWeightMinus5.setOnClickListener(v -> adjustWeight(-5.0f));

        // Add button
        btnAdd.setOnClickListener(v -> addWeight());
    }

    /**
     * Adds a new weight entry to the database after validating user input.
     * Performs comprehensive validation including date selection, weight format,
     * weight range (50-1000 lbs), and duplicate date checking.
     */
    private void addWeight() {
        // Get the ISO formatted date from the date picker tag
        String dateIso = (String) etDate.getTag();
        // Get weight input and trim whitespace
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";

        // Validate that user has selected a date
        if (TextUtils.isEmpty(dateIso)) {
            toast("Please select a date");
            return;
        }

        // Validate that weight field is not empty
        if (TextUtils.isEmpty(weightStr)) {
            tilWeight.setError("Weight is required");
            etWeight.requestFocus();
            return;
        }

        // Parse weight string to float, handle invalid number format
        float weight;
        try {
            weight = Float.parseFloat(weightStr);
        } catch (NumberFormatException ex) {
            tilWeight.setError("Please enter a valid number");
            etWeight.requestFocus();
            return;
        }

        // Validate weight is within reasonable range (50-1000 lbs)
        if (weight < 50 || weight > 1000) {
            tilWeight.setError("Weight must be between 50-1000 lbs");
            etWeight.requestFocus();
            return;
        }

        // Check if weight entry already exists for this date
        if (repo.hasWeightEntry(userId, dateIso)) {
            tilWeight.setError(null);
            toast("Weight already logged for this date");
            return;
        }

        // Attempt to save weight entry to database
        long rowId = repo.addWeight(userId, dateIso, weight);
        if (rowId > 0) {
            // Clear any error messages and show success feedback
            tilWeight.setError(null);
            toast("Weight logged successfully!");
            clearInputs();
            refresh();
        } else {
            toast("Could not add weight");
        }
    }

    private void setupWeightInput(List<WeightsRepository.WeightDTO> weightHistory) {
        // Set default weight based on latest entry
        if (weightHistory.isEmpty()) {
            etWeight.setText("150.0");
        } else {
            WeightsRepository.WeightDTO lastWeight = weightHistory.get(0);
            // Format the weight to ensure proper decimal display
            String weightText = String.format("%.1f", lastWeight.weightLb);
            etWeight.setText(weightText);
        }

        // Add keyboard action listener
        etWeight.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                addWeight();
                return true;
            }
            return false;
        });

        // Real-time weight input validation
        etWeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                validateWeightInput(s.toString());
            }
        });
    }

    /**
     * Sets up the date hint to show today's date
     */
    private void setupDateHint() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String todayString = displayFormat.format(today.getTime());
        etDate.setHint(todayString);
    }

    /**
     * Adjusts the weight input by the given amount
     * 
     * @param amount the amount to adjust the weight by
     */
    private void adjustWeight(float amount) {
        String currentText = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        if (TextUtils.isEmpty(currentText)) {
            etWeight.setText("150.0");
            return;
        }

        try {
            float currentWeight = Float.parseFloat(currentText);
            float newWeight = currentWeight + amount;

            // Ensure value stays within bounds
            if (newWeight < 50) {
                newWeight = 50;
            } else if (newWeight > 1000) {
                newWeight = 1000;
            }

            etWeight.setText(String.format("%.1f", newWeight));
        } catch (NumberFormatException e) {
            etWeight.setText("150.0");
        }
    }

    /**
     * Validates the weight input
     * 
     * @param input the weight input
     */
    private void validateWeightInput(String input) {
        if (TextUtils.isEmpty(input)) {
            tilWeight.setError(null);
            return;
        }

        try {
            float weight = Float.parseFloat(input);
            if (weight < 50 || weight > 1000) {
                tilWeight.setError("Weight must be between 50-1000 lbs");
            } else {
                tilWeight.setError(null);
            }
        } catch (NumberFormatException e) {
            tilWeight.setError("Please enter a valid number");
        }
    }

    /**
     * Shows the date picker dialog and updates the date input field
     */
    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    // Format display with month name
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                    String displayDate = displayFormat.format(selectedDate.getTime());
                    etDate.setText(displayDate);

                    // Store ISO date for database
                    String isoDate = String.format("%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    etDate.setTag(isoDate);
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        datePickerDialog.show();
    }

    /**
     * Called when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Refreshes the weight history list
     */
    private void refresh() {
        List<WeightsRepository.WeightDTO> items = repo.getWeightHistory(userId);
        adapter.replace(items);
    }

    /**
     * Clears the weight input fields
     */
    private void clearInputs() {
        // Get the latest weight to use as default
        List<WeightsRepository.WeightDTO> weightHistory = repo.getWeightHistory(userId);
        if (weightHistory.isEmpty()) {
            etWeight.setText("150.0");
        } else {
            WeightsRepository.WeightDTO lastWeight = weightHistory.get(0);
            String weightText = String.format("%.1f", lastWeight.weightLb);
            etWeight.setText(weightText);
        }

        // Clear date fields
        etDate.setText("");
        etDate.setTag(null);
    }

    /**
     * Shows a toast message
     * 
     * @param s the message to show
     */
    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
