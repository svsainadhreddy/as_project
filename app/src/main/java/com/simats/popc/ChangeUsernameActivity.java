package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeUsernameActivity extends AppCompatActivity {

    EditText etNewUsername;
    Button btnSave;

    ImageButton btnback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);
        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etNewUsername = findViewById(R.id.etNewUsername);
        btnSave = findViewById(R.id.btnSaveUsername);
        btnback = findViewById(R.id.btnBack);
        btnback.setOnClickListener(view ->
                startActivity(new Intent(ChangeUsernameActivity.this, SettingsActivity.class)));

        btnSave.setOnClickListener(v -> {
            String newUsername = etNewUsername.getText().toString().trim();

            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Enter new username", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Map<String, String> body = new HashMap<>();
            body.put("username", newUsername);

            Call<Void> call = apiService.changeUsername("Token " + SharedPrefManager.getInstance(this).getToken(), body);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ChangeUsernameActivity.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ChangeUsernameActivity.this, "Failed to update username", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ChangeUsernameActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
