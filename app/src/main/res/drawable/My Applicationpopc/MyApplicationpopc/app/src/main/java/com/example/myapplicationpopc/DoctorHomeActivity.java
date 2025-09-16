package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DoctorHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // hide default toolbar
        }

        // Back button → Go to login (SecondActivity)
        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                Intent intent = new Intent(DoctorHomeActivity.this, SecondActivity.class);
                startActivity(intent);
                finish();
            });
            // Add Patient → open AddPatientActivity
            Button btnAddPatient = findViewById(R.id.btnAddPatient);
            btnAddPatient.setOnClickListener(v -> {
                Intent intent = new Intent(DoctorHomeActivity.this, AddPatientActivity.class);
                startActivity(intent);
            });

        }

        // Menu icon → popup menu
        ImageView menuIcon = findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(DoctorHomeActivity.this, menuIcon);
            popup.getMenuInflater().inflate(R.menu.menu_doctor_home, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_profile) {
                    // Navigate to profile page
                    Intent intent = new Intent(DoctorHomeActivity.this, DoctorProfileActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.action_logout) {
                    // Navigate back to login page
                    Intent intent = new Intent(DoctorHomeActivity.this, SecondActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popup.show();
        });

        // List of Patient → open ListPatientActivity
        Button btnListPatients = findViewById(R.id.btnListPatients);
        btnListPatients.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, ListPatientActivity.class);
            startActivity(intent);
        });

        // Add Patient → open AddPatientActivity
        Button btnAddPatient = findViewById(R.id.btnAddPatient);
        btnAddPatient.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, AddPatientActivity.class);
            startActivity(intent);
        });
    }
}
