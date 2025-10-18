package com.example.kc_weight_tracker;
import com.example.kc_weight_tracker.R;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnSignIn, btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(v->{
            goToWeightTracking();
        });

        btnSignIn.setOnClickListener(v->{
            goToWeightTracking();
        });
    }

    /// A temporary navigation to mimic successful login
    /// will be wired in next assignment
    private void goToWeightTracking() {
        startActivity(new Intent(this, WeightTrackingActivity.class));
        finish();
    }
}