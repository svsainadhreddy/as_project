package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;


import android.widget.ImageButton;



public class PatientManagementActivity extends AppCompatActivity {

    Button btnAddPatient, btnViewPatient, btnEditPatient;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_management); // your xml name

        btnBack = findViewById(R.id.btnBack);
        btnAddPatient = findViewById(R.id.btnAddPatient);
        btnViewPatient = findViewById(R.id.btnViewPatient);
        btnEditPatient = findViewById(R.id.btnEditPatient);

        btnBack.setOnClickListener(v -> finish());

        btnAddPatient.setOnClickListener(v -> startActivity(new Intent(this, AddPatientActivity.class)));

        btnViewPatient.setOnClickListener(v -> {
            Intent i = new Intent(this, ViewPatientListActivity.class);
            i.putExtra("mode", "view");
            startActivity(i);
        });

        btnEditPatient.setOnClickListener(v -> {
            Intent i = new Intent(this, ViewPatientListActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });
    }
}
