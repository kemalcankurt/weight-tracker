package com.example.kc_weight_tracker;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class WeightTrackingActivity extends AppCompatActivity {
    private TextInputEditText etDate, etWeight;
    private MaterialButton btnAdd;
    // Grid
    private RecyclerView rvGrid;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_tracking);

        MaterialToolbar bar = findViewById(R.id.topAppBar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Weight History");
        }

        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        btnAdd = findViewById(R.id.btnAdd);
        /// commented out but will enable for dynamic weight binding
//        rvGrid = findViewById(R.id.rvGrid);
    }
}
