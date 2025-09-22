package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PatientDemographicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_demographics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide default toolbar
        }

        // Back button → return to AddPatientActivity
        ImageButton backBtn = findViewById(R.id.ivBack);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDemographicActivity.this, AddPatientActivity.class);
            startActivity(intent);
            finish();
        });

        // Mandatory RadioGroups
        RadioGroup rgAge = findViewById(R.id.rgAge);
        RadioGroup rgSex = findViewById(R.id.rgSex);
        RadioGroup rgBmi = findViewById(R.id.rgBmi);
        RadioGroup rgSmoking = findViewById(R.id.rgSmoking);
        RadioGroup rgAlcohol = findViewById(R.id.rgAlcohol);

        // Next button → open MedicalHistoryActivity
        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if (rgAge.getCheckedRadioButtonId() == -1 ||
                    rgSex.getCheckedRadioButtonId() == -1 ||
                    rgBmi.getCheckedRadioButtonId() == -1 ||
                    rgSmoking.getCheckedRadioButtonId() == -1 ||
                    rgAlcohol.getCheckedRadioButtonId() == -1) {

                Toast.makeText(this, "Please select all fields", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(PatientDemographicActivity.this, MedicalHistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
