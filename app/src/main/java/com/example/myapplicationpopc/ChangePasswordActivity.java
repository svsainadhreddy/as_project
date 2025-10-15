package com.example.myapplicationpopc;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private ImageView ivToggleOldPassword, ivToggleNewPassword, ivToggleConfirmPassword;
    private boolean isOldVisible = false, isNewVisible = false, isConfirmVisible = false;
    private ImageButton btnBack;
    private Button btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        ivToggleOldPassword = findViewById(R.id.ToggleOldPassword);
        ivToggleNewPassword = findViewById(R.id.ToggleNewPassword);
        ivToggleConfirmPassword = findViewById(R.id.ToggleConfirmPassword);

        btnBack = findViewById(R.id.btnBack);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Save button
        btnSavePassword.setOnClickListener(v -> changePassword());

        // Toggle password visibility
        ivToggleOldPassword.setOnClickListener(v -> togglePasswordVisibility(etOldPassword, ivToggleOldPassword, 1));
        ivToggleNewPassword.setOnClickListener(v -> togglePasswordVisibility(etNewPassword, ivToggleNewPassword, 2));
        ivToggleConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, ivToggleConfirmPassword, 3));
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon, int fieldType) {
        boolean visible;
        if (fieldType == 1) {
            visible = isOldVisible = !isOldVisible;
        } else if (fieldType == 2) {
            visible = isNewVisible = !isNewVisible;
        } else {
            visible = isConfirmVisible = !isConfirmVisible;
        }

        if (visible) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_eye_open);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_eye_closed);
        }

        editText.setSelection(editText.getText().length());
    }

    private void changePassword() {
        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Map<String, String> body = new HashMap<>();
        body.put("old_password", oldPass);
        body.put("new_password", newPass);

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Void> call = apiService.changePassword("Token " + token, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
