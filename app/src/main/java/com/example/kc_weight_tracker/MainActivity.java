package com.example.kc_weight_tracker;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kc_weight_tracker.repository.UserRepository;
import com.example.kc_weight_tracker.utility.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnSignIn, btnCreateAccount;

    private UserRepository users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        users = new UserRepository(this);

        /// if user is already logged in, bypass login / sign-in page and go to
        /// weight history
        if (SessionManager.isLoggedIn(this)) {
            goToHomeActivity();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(v -> {
            createAccount();
        });

        btnSignIn.setOnClickListener(v -> {
            signIn();
        });
    }

    /**
     * Navigate to the home activity
     */
    private void goToHomeActivity() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    /**
     * Create an account
     */
    private void createAccount() {
        String userName = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        if (userName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        long id = users.createUser(userName, password);
        if (id > 0) {
            getSharedPreferences("session", MODE_PRIVATE)
                    .edit().putLong("user_id", id).putString("username", userName).apply();
            Toast.makeText(this, "Account created. Welcome, " + userName + "!", Toast.LENGTH_SHORT).show();
            goToHomeActivity();
        } else {
            Toast.makeText(this, "Username already exists or invalid.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sign in the user
     */
    private void signIn() {
        String u = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String p = Objects.requireNonNull(etPassword.getText()).toString();

        if (u.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Enter your username and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        long userId = users.authenticate(u, p);
        if (userId > 0) {
            getSharedPreferences("session", MODE_PRIVATE)
                    .edit().putLong("user_id", userId).putString("username", u).apply();
            goToHomeActivity();
        } else {
            Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
        }
    }
}