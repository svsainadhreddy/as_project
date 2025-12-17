package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecondActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button loginButton, registerButton, forgotBtn;
    private ImageView ivTogglePassword;
    private ApiService apiService;

    private TextInputLayout passwordInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        forgotBtn = findViewById(R.id.forgotpassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        apiService = ApiClient.getClient().create(ApiService.class);

        setupPasswordToggle();
        setupInputWatchers();

        loginButton.setOnClickListener(v -> loginDoctor());
        registerButton.setOnClickListener(v -> startActivity(new Intent(SecondActivity.this, RegisterActivity.class)));

        forgotBtn.setOnClickListener(v -> handleForgotPasswordFromUsername());
    }

    // -------------------- BUTTON ENABLE / DISABLE --------------------

    private void setupInputWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonsState();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etUsername.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);

        updateButtonsState(); // Initial state
    }

    private void updateButtonsState() {
        String u = etUsername.getText().toString().trim();
        String p = etPassword.getText().toString().trim();

        // Login button: enabled only if BOTH fields not empty
        boolean loginEnabled = !u.isEmpty() && !p.isEmpty();
        loginButton.setEnabled(loginEnabled);
        loginButton.setAlpha(loginEnabled ? 1f : 0.5f);

        // Forgot password: enabled only if username not empty
        boolean forgotEnabled = !u.isEmpty();
        forgotBtn.setEnabled(forgotEnabled);
        forgotBtn.setAlpha(forgotEnabled ? 1f : 0.5f);
    }

    // -------------------- PASSWORD TOGGLE --------------------

    private void setupPasswordToggle() {
        if (ivTogglePassword != null) {
            ivTogglePassword.setOnClickListener(v -> togglePasswordField());
            return;
        }

        ViewParent parent = etPassword.getParent();
        if (parent instanceof TextInputLayout) {
            passwordInputLayout = (TextInputLayout) parent;
            passwordInputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        }
    }

    private void togglePasswordField() {
        if (etPassword == null) return;

        if (passwordInputLayout != null) return; // Material Layout handles it

        int currentType = etPassword.getInputType();

        final int visible = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        final int hidden = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;

        if (currentType == visible) {
            etPassword.setInputType(hidden);
            ivTogglePassword.setImageResource(R.drawable.ic_eye_closed);
        } else {
            etPassword.setInputType(visible);
            ivTogglePassword.setImageResource(R.drawable.ic_eye_open);
        }
        etPassword.setSelection(etPassword.length());
    }

    // -------------------- LOGIN --------------------

    private void loginDoctor() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) return;

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        apiService.loginDoctor(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().get("token");
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

    // -------------------- FORGOT PASSWORD --------------------

    private void handleForgotPasswordFromUsername() {
        String identifier = etUsername.getText().toString().trim();

        if (identifier.isEmpty()) return;

        Intent i = new Intent(SecondActivity.this, OtpVerifyActivity.class);
        i.putExtra("identifier", identifier);
        startActivity(i);

        sendOtpInBackground(identifier);
    }

    private void sendOtpInBackground(String identifier) {
        Map<String, String> body = new HashMap<>();
        body.put("identifier", identifier);

        apiService.forgotPassword(body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> resp) {}
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Toast.makeText(SecondActivity.this, "Failed to send OTP: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
