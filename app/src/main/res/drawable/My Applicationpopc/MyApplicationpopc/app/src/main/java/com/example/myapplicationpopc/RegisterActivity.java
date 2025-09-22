package com.example.myapplicationpopc;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etDoctorId, etName, etPhone, etEmail, etAge, etSpecialization, etUsername, etPassword;
    private Button btnSave;
    private ImageView ivTogglePassword, ivBack;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hide default ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        ivBack = findViewById(R.id.ivBack);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        etDoctorId = findViewById(R.id.etDoctorId);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        etSpecialization = findViewById(R.id.etSpecialization);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);

        // Back button click
        ivBack.setOnClickListener(v -> finish());

        // Toggle password visibility
        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivTogglePassword.setImageResource(R.drawable.ic_eye_closed);
                isPasswordVisible = false;
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivTogglePassword.setImageResource(R.drawable.ic_eye_open);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.length());
        });

        // Save button click
        btnSave.setOnClickListener(v -> saveData());
    }

    private void saveData() {
        String doctorId = etDoctorId.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String specialization = etSpecialization.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (doctorId.isEmpty() || name.isEmpty() || phone.isEmpty() || email.isEmpty()
                || age.isEmpty() || specialization.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            String message = "DoctorId: " + doctorId +
                    "\nName: " + name +
                    "\nPhone: " + phone +
                    "\nEmail: " + email +
                    "\nAge: " + age +
                    "\nSpecialization: " + specialization +
                    "\nUsername: " + username;
            Toast.makeText(this, "Saved:\n" + message, Toast.LENGTH_LONG).show();
        }
    }
}
