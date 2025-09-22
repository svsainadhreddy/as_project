package com.example.myapplicationpopc;


import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.*;

public class RegisterActivity extends AppCompatActivity {
    EditText etDoctorId, etName, etPhone, etEmail, etAge, etSpecialization, etUsername, etPassword, etConfirmPassword;
    Spinner spinnerGender;
    Button btnSave;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
          // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etDoctorId = findViewById(R.id.etDoctorId);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        etSpecialization = findViewById(R.id.etSpecialization);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSave);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnSave.setOnClickListener(v -> registerDoctor());
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Male", "Female", "Other"} // options
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ImageView ivTogglePassword2 = findViewById(R.id.ivTogglePasswords);

        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Show password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                // Hide password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            etPassword.setSelection(etPassword.getText().length()); // move cursor to end
        });
        ivTogglePassword2.setOnClickListener(v -> {
            if (etConfirmPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Show password
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword2.setImageResource(R.drawable.ic_eye_open);
            } else {
                // Hide password
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword2.setImageResource(R.drawable.ic_eye_closed);
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length()); // move cursor to end
        });

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
        body.put("specialization", etSpecialization.getText().toString());
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
