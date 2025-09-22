package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AddPatientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide default toolbar
        }

        // Back arrow → return to DoctorHomeActivity
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddPatientActivity.this, DoctorHomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Next button → go to PatientDemographicActivity
        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(AddPatientActivity.this, PatientDemographicActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
