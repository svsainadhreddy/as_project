package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.*;

public class RegisterActivity extends AppCompatActivity {
    EditText etDoctorId, etName, etPhone, etEmail, etAge, etUsername, etPassword, etConfirmPassword;
    Spinner spinnerGender, spinnerSpecialization;
    Button btnSave;
    ImageButton btnBack;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hide toolbar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 🔹 Find all views
        etDoctorId = findViewById(R.id.etDoctorId);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerSpecialization = findViewById(R.id.etSpecialization);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.ivBack);
        apiService = ApiClient.getClient().create(ApiService.class);

        // 🔹 Back button
        btnBack.setOnClickListener(view -> finish());

        // 🔹 Gender dropdown
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Male", "Female", "Other"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // 🔹 Specialization dropdown
        ArrayAdapter<String> specializationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Anesthesia"});
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialization.setAdapter(specializationAdapter);

        // 🔹 Password toggle
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ImageView ivTogglePassword2 = findViewById(R.id.ivTogglePasswords);

        ivTogglePassword.setOnClickListener(v -> togglePassword(etPassword, ivTogglePassword));
        ivTogglePassword2.setOnClickListener(v -> togglePassword(etConfirmPassword, ivTogglePassword2));

        // 🔹 Enter key navigation
        setEditTextNext(etDoctorId, etName);
        setEditTextNext(etName, etPhone);
        setEditTextNext(etPhone, etEmail);
        setEditTextNext(etEmail, etAge);
        setEditTextNext(etAge, etUsername);
        setEditTextNext(etUsername, etPassword);
        setEditTextNext(etPassword, etConfirmPassword);
        etConfirmPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // 🔹 Save button
        btnSave.setOnClickListener(v -> {
            if (validateFields()) registerDoctor();
        });
    }

    private void togglePassword(EditText et, ImageView iv) {
        if (et.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            iv.setImageResource(R.drawable.ic_eye_open);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            iv.setImageResource(R.drawable.ic_eye_closed);
        }
        et.setSelection(et.getText().length());
    }

    private void setEditTextNext(EditText current, EditText next) {
        current.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                next.requestFocus();
                return true;
            }
            return false;
        });
    }

    private boolean validateFields() {
        String doctorId = etDoctorId.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (doctorId.isEmpty() || name.isEmpty() || phone.isEmpty() || email.isEmpty()
                || age.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!name.matches("[a-zA-Z ]+")) {
            etName.setError("Name must contain only alphabets");
            etName.requestFocus();
            return false;
        }

        if (!email.matches(Patterns.EMAIL_ADDRESS.pattern())) {
            etEmail.setError("Invalid email address");
            etEmail.requestFocus();
            return false;
        }

        if (!phone.matches("[6-9][0-9]{9}")) {
            etPhone.setError("Enter valid 10-digit Indian phone number");
            etPhone.requestFocus();
            return false;
        }

        if (!age.matches("\\d+")) {
            etAge.setError("Age must be a number");
            etAge.requestFocus();
            return false;
        }

        if (!username.matches("[a-zA-Z0-9]+")) {
            etUsername.setError("Username must be alphanumeric");
            etUsername.requestFocus();
            return false;
        }

        if (!isStrongPassword(password)) {
            etPassword.setError("Password must be min 8 chars, alphanumeric with letters and digits");
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
    }

    private void registerDoctor() {
        Map<String, String> body = new HashMap<>();
        body.put("doctor_id", etDoctorId.getText().toString());
        body.put("name", etName.getText().toString());
        body.put("username", etUsername.getText().toString());
        body.put("email", etEmail.getText().toString());
        body.put("phone", etPhone.getText().toString());
        body.put("age", etAge.getText().toString());
        body.put("gender", spinnerGender.getSelectedItem().toString());
        body.put("specialization", spinnerSpecialization.getSelectedItem().toString());
        body.put("password", etPassword.getText().toString());
        body.put("password2", etConfirmPassword.getText().toString());

        Call<ResponseBody> call = apiService.registerDoctor(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, SecondActivity.class));
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(RegisterActivity.this, "Failed: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
