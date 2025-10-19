package com.example.kc_weight_tracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.kc_weight_tracker.repository.UserRepository;
import com.example.kc_weight_tracker.repository.WeightsRepository;
import com.example.kc_weight_tracker.repository.WeightGoalRepository;
import com.example.kc_weight_tracker.utility.BMICalculator;

import com.example.kc_weight_tracker.utility.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

/**
 * SetGoalBottomSheetDialog is a dialog that allows the user to set a weight goal.
 */
public class SetGoalBottomSheetDialog extends BottomSheetDialogFragment {

    // Interface for the listener
    public interface OnGoalSetListener {
        void onGoalSet();
    }

    private OnGoalSetListener listener;
    private Context context;

    // UI Components
    private ImageView btnClose;
    private TextView tvCurrentWeightDisplay, tvCurrentBMI;
    private TextView tvTargetWeightDisplay, tvTargetBMI, tvWeightChange, tvBMICategory;
    private NumberPicker npWeight;
    private TextInputEditText etTargetDate;
    private MaterialButton btnCancel, btnSaveGoal;
    private MaterialButton btnDecreaseWeight, btnDecreaseWeightSmall, btnIncreaseWeightSmall, btnIncreaseWeight;

    // Data
    private Float currentWeight;
    private Double userHeight = null; // Will be loaded from database, null if not set
    private LocalDate selectedDate;

    public static SetGoalBottomSheetDialog newInstance(OnGoalSetListener listener) {
        SetGoalBottomSheetDialog dialog = new SetGoalBottomSheetDialog();
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_set_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        loadCurrentData();
        setupClickListeners();
        setupTextWatchers();
        updatePreview();
    }

    private void initializeViews(View view) {
        btnClose = view.findViewById(R.id.btnClose);
        tvCurrentWeightDisplay = view.findViewById(R.id.tvCurrentWeightDisplay);
        tvCurrentBMI = view.findViewById(R.id.tvCurrentBMI);
        tvTargetWeightDisplay = view.findViewById(R.id.tvTargetWeightDisplay);
        tvTargetBMI = view.findViewById(R.id.tvTargetBMI);
        tvWeightChange = view.findViewById(R.id.tvWeightChange);
        tvBMICategory = view.findViewById(R.id.tvBMICategory);
        npWeight = view.findViewById(R.id.npWeight);
        etTargetDate = view.findViewById(R.id.etTargetDate);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSaveGoal = view.findViewById(R.id.btnSaveGoal);
        btnDecreaseWeight = view.findViewById(R.id.btnDecreaseWeight);
        btnDecreaseWeightSmall = view.findViewById(R.id.btnDecreaseWeightSmall);
        btnIncreaseWeightSmall = view.findViewById(R.id.btnIncreaseWeightSmall);
        btnIncreaseWeight = view.findViewById(R.id.btnIncreaseWeight);
    }

    private void loadCurrentData() {
        // Get current weight from repository
        WeightsRepository weightsRepo = new WeightsRepository(context);
        UserRepository userRepo = new UserRepository(context);
        long userId = SessionManager.userId(context);

        currentWeight = weightsRepo.getLatestWeight(userId);
        userHeight = userRepo.getUserHeight(userId); // Load actual user height

        if (currentWeight != null) {
            tvCurrentWeightDisplay.setText(String.format("%.1f", currentWeight));

            // Only calculate BMI if height is set
            if (userHeight != null && userHeight > 0) {
                double currentBMI = BMICalculator.calculateBMI(currentWeight, userHeight);
                tvCurrentBMI.setText(String.format("%.1f", currentBMI));
            } else {
                tvCurrentBMI.setText("--");
            }
        } else {
            tvCurrentWeightDisplay.setText("--");
            tvCurrentBMI.setText("--");
        }

        // Set default target date (1 month from now)
        selectedDate = LocalDate.now().plusMonths(1);
        updateDateDisplay();

        // Initialize NumberPicker
        setupNumberPicker();
    }

    private void setupNumberPicker() {
        // Set up NumberPicker for weight selection (100-300 lbs)
        npWeight.setMinValue(100);
        npWeight.setMaxValue(300);
        npWeight.setValue(175); // Default value

        // Set up change listener
        npWeight.setOnValueChangedListener((picker, oldVal, newVal) -> {
            updatePreview();
        });

        // Set initial value based on current weight if available
        if (currentWeight != null) {
            int currentWeightInt = Math.round(currentWeight);
            if (currentWeightInt >= 100 && currentWeightInt <= 300) {
                npWeight.setValue(currentWeightInt);
            }
        }
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnCancel.setOnClickListener(v -> dismiss());

        btnSaveGoal.setOnClickListener(v -> saveGoal());

        etTargetDate.setOnClickListener(v -> showDatePicker());

        // Quick adjust buttons
        btnDecreaseWeight.setOnClickListener(v -> {
            int currentValue = npWeight.getValue();
            if (currentValue >= 105) {
                npWeight.setValue(currentValue - 5);
            }
        });

        btnDecreaseWeightSmall.setOnClickListener(v -> {
            int currentValue = npWeight.getValue();
            if (currentValue > 100) {
                npWeight.setValue(currentValue - 1);
            }
        });

        btnIncreaseWeightSmall.setOnClickListener(v -> {
            int currentValue = npWeight.getValue();
            if (currentValue < 300) {
                npWeight.setValue(currentValue + 1);
            }
        });

        btnIncreaseWeight.setOnClickListener(v -> {
            int currentValue = npWeight.getValue();
            if (currentValue <= 295) {
                npWeight.setValue(currentValue + 5);
            }
        });
    }

    private void setupTextWatchers() {
        // No longer needed since we're using NumberPicker
    }

    private void updatePreview() {
        if (currentWeight == null) {
            return;
        }

        double targetWeight = npWeight.getValue();

        // Update target weight display
        tvTargetWeightDisplay.setText(String.format("%.0f", targetWeight));

        // Only calculate BMI if height is set
        if (userHeight != null && userHeight > 0) {
            // Calculate target BMI
            double targetBMI = BMICalculator.calculateBMI(targetWeight, userHeight);
            tvTargetBMI.setText(String.format("%.1f", targetBMI));

            // Calculate weight change
            double weightChange = targetWeight - currentWeight;
            String changeText;
            if (weightChange > 0) {
                changeText = String.format("Gain %.1f lb", weightChange);
            } else if (weightChange < 0) {
                changeText = String.format("Lose %.1f lb", Math.abs(weightChange));
            } else {
                changeText = "Maintain weight";
            }
            tvWeightChange.setText(changeText);

            // Update BMI category
            String bmiCategory = BMICalculator.getBMICategory(targetBMI);
            tvBMICategory.setText(bmiCategory);
        } else {
            // Show simple message when height is not set
            tvTargetBMI.setText("--");
            tvWeightChange.setText("Set height in Settings for BMI");
            tvBMICategory.setText("--");
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        String month = selectedDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        int day = selectedDate.getDayOfMonth();
        int year = selectedDate.getYear();
        etTargetDate.setText(String.format("%s %d, %d", month, day, year));
    }

    private void saveGoal() {
        double targetWeight = npWeight.getValue();

        // Validate target weight with better error messages (only if height is set)
        if (currentWeight != null && userHeight != null && userHeight > 0) {
            String validationMessage = BMICalculator.getValidationMessage(currentWeight, targetWeight, userHeight);
            if (validationMessage != null) {
                Toast.makeText(context, validationMessage, Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Save goal to database
        WeightGoalRepository goalRepo = new WeightGoalRepository(context);
        long userId = SessionManager.userId(context);

        long newGoalId = goalRepo.upsertGoal(userId, (float) targetWeight, selectedDate.toString());
        if (newGoalId > 0) {
            Toast.makeText(context, "Goal saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error while saving your goal.", Toast.LENGTH_SHORT).show();
        }


        if (listener != null) {
            listener.onGoalSet();
        }

        dismiss();
    }
}
