package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

    EditText etOldPassword, etNewPassword;
    Button btnSave;
    ImageButton btnback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSave = findViewById(R.id.btnSavePassword);
        btnback = findViewById(R.id.btnBack);
        btnback.setOnClickListener(view ->
                startActivity(new Intent(ChangePasswordActivity.this, SettingsActivity.class)));

        btnSave.setOnClickListener(v -> {
            String oldPass = etOldPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Enter both passwords", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Map<String, String> body = new HashMap<>();
            body.put("old_password", oldPass);
            body.put("new_password", newPass);

            Call<Void> call = apiService.changePassword("Token " + SharedPrefManager.getInstance(this).getToken(), body);
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
                    Toast.makeText(ChangePasswordActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
