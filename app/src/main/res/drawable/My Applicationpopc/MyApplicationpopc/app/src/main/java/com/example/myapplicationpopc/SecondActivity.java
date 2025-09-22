package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_second);

        passwordEditText = findViewById(R.id.password);
        togglePassword = findViewById(R.id.togglePassword);

        // 👁 Toggle password visibility
        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.ic_eye_closed);
                isPasswordVisible = false;
            } else {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.ic_eye_open);
                isPasswordVisible = true;
            }
            passwordEditText.setSelection(passwordEditText.length());
        });

        // ✅ Login Button Logic with Validation
        findViewById(R.id.loginButton).setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            } else if (password.equals("admin123")) {
                Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SecondActivity.this, HomeAdminActivity.class));
                finish();
            } else if (password.equals("doctor123")) {
                Toast.makeText(this, "Welcome Doctor!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SecondActivity.this, DoctorHomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid Password!", Toast.LENGTH_SHORT).show();
            }
        });

        // Register button
        findViewById(R.id.registerButton).setOnClickListener(v ->
                startActivity(new Intent(SecondActivity.this, RegisterActivity.class)));

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }
}
