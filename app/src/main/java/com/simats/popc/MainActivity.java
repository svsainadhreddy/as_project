package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.simats.popc.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        if (SharedPrefManager.getInstance(this).getToken() != null) {
            // User is logged in, go directly to DoctorHomeActivity
            Intent intent = new Intent(MainActivity.this, DoctorHomeActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity so back button doesn't return here
            return; // Stop further execution
        }

        setContentView(R.layout.activity_main);

        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Local button variables
        AppCompatButton btnLogin = findViewById(R.id.btnLogin);
        AppCompatButton btnregister = findViewById(R.id.btnSignin);

        // Use lambda for click listeners
        btnLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        });

        btnregister.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
