package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.utils.SharedPrefManager;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout btnChangeUsername, btnChangePassword, btnRateUs, btnLogout;
    ImageButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnChangeUsername = findViewById(R.id.btnChangeUsername);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnRateUs = findViewById(R.id.btnRateUs);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        //Back button
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, DoctorHomeActivity.class);
            startActivity(intent);
        });
        // Change Username
        btnChangeUsername.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangeUsernameActivity.class);
            startActivity(intent);
        });

        // Change Password
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Rate Us
        btnRateUs.setOnClickListener(v ->
                Toast.makeText(this, "Redirecting to Play Store...", Toast.LENGTH_SHORT).show()
        );

        // Logout
        btnLogout.setOnClickListener(v -> {
            // Use logout() instead of clear()
            SharedPrefManager.getInstance(getApplicationContext()).logout();

            Intent intent = new Intent(SettingsActivity.this, SecondActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }
}
