package com.example.myapplicationpopc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.*;
import android.widget.*;
import retrofit2.*;

public class SecondActivity extends AppCompatActivity {


        EditText etUsername, etPassword;
        Button loginButton, registerButton;

        ApiService apiService;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_second);
            // Hide toolbar
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            etUsername = findViewById(R.id.username);
            etPassword = findViewById(R.id.password);
            loginButton = findViewById(R.id.loginButton);
            registerButton = findViewById(R.id.registerButton);

            apiService = ApiClient.getClient().create(ApiService.class);

            loginButton.setOnClickListener(v -> loginDoctor());
            registerButton.setOnClickListener(v -> startActivity(new Intent(SecondActivity.this, RegisterActivity.class)));
            ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);

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
        }

        private void loginDoctor() {
            Map<String, String> body = new HashMap<>();
            body.put("username", etUsername.getText().toString());
            body.put("password", etPassword.getText().toString());

            Call<Map<String, String>> call = apiService.loginDoctor(body);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String token = response.body().get("token");
                        String username = etUsername.getText().toString().trim();
                        Toast.makeText(SecondActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                        // TODO: Save token in SharedPreferences
                        SharedPrefManager.getInstance(SecondActivity.this)
                                .saveLoginData(token, "", username);
                        startActivity(new Intent(SecondActivity.this, DoctorHomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SecondActivity.this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(SecondActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
