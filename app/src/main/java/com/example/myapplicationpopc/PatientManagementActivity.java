package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;


import android.widget.ImageButton;



public class PatientManagementActivity extends AppCompatActivity {

    Button btnAddPatient, btnViewPatient, btnEditPatient,btnDeletePatient;
    ImageButton btn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_management);
        // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        btnAddPatient = findViewById(R.id.btnAddPatient);
        btnViewPatient = findViewById(R.id.btnViewPatient);
        btnEditPatient = findViewById(R.id.btnEditPatient);
        btnDeletePatient = findViewById(R.id. btnDeletePatient);
        btn1 = findViewById(R.id.btnBack);




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
        btnDeletePatient.setOnClickListener(v -> {
            Intent i = new Intent(this, DeletePatientActivity.class);
            i.putExtra("mode", "Delete");
            startActivity(i);
        });
        //back to DoctorHomeActivity
        btn1.setOnClickListener(v -> {
            Intent i = new Intent(this, DoctorHomeActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });
    }
}
