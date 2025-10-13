package com.example.myapplicationpopc;

import android.content.Intent;
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

public class SecondActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button loginButton, registerButton;
    ImageView ivTogglePassword;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 🔹 If user already logged in, skip login screen
        // if (SharedPrefManager.getInstance(this).getToken() != null) {
        //   startActivity(new Intent(SecondActivity.this, DoctorHomeActivity.class));
        //  finish();
        //  return;
        // }

        // Initialize views
        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Login click
        loginButton.setOnClickListener(v -> loginDoctor());

        // Register click
        registerButton.setOnClickListener(v ->
                startActivity(new Intent(SecondActivity.this, RegisterActivity.class))
        );

        // Password visibility toggle
        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() ==
                    (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Show password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                // Hide password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            etPassword.setSelection(etPassword.getText().length());
        });
    }

    private void loginDoctor() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        Call<Map<String, String>> call = apiService.loginDoctor(body);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().get("token");
                    Toast.makeText(SecondActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();

                    // Save token and username
                    SharedPrefManager.getInstance(SecondActivity.this)
                            .saveLoginData(token, "", username);

                    // Move to home
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
