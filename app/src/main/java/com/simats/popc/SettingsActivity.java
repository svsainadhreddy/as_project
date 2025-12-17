package com.simats.popc;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * SettingsActivity — improved for reliable logout after delete.
 * Handles OkHttp "HTTP 204 had non-zero Content-Length" protocol error as success.
 */
public class SettingsActivity extends AppCompatActivity {

    private LinearLayout btnChangeUsername, btnChangePassword, btnRateUs, btnLogout, btnDeleteAcc;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private ApiService apiService;
    private boolean isRequestRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // bind views (progressBar optional)
        btnChangeUsername = safeFindViewById(R.id.btnChangeUsername);
        btnChangePassword = safeFindViewById(R.id.btnChangePassword);
        btnRateUs = safeFindViewById(R.id.btnRateUs);
        btnLogout = safeFindViewById(R.id.btnLogout);
        btnBack = safeFindViewById(R.id.btnBack);
        btnDeleteAcc = safeFindViewById(R.id.deleteacc);

        apiService = ApiClient.getClient().create(ApiService.class);

        // click handlers
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            if (isRequestRunning) return;
            startActivity(new Intent(SettingsActivity.this, DoctorHomeActivity.class));
        });

        if (btnChangeUsername != null) btnChangeUsername.setOnClickListener(v -> {
            if (isRequestRunning) return;
            startActivity(new Intent(SettingsActivity.this, ChangeUsernameActivity.class));
        });

        if (btnChangePassword != null) btnChangePassword.setOnClickListener(v -> {
            if (isRequestRunning) return;
            startActivity(new Intent(SettingsActivity.this, ChangePasswordActivity.class));
        });

        if (btnRateUs != null) btnRateUs.setOnClickListener(v -> {
            if (isRequestRunning) return;
            Toast.makeText(this, "Redirecting to Play Store...", Toast.LENGTH_SHORT).show();
        });

        if (btnLogout != null) btnLogout.setOnClickListener(v -> {
            if (isRequestRunning) return;
            performImmediateLocalLogout("Logged out");
        });

        if (btnDeleteAcc != null) btnDeleteAcc.setOnClickListener(v -> {
            if (isRequestRunning) return;
            showDeleteConfirmationDialog();
        });
    }

    private <T extends View> T safeFindViewById(int id) {
        try {
            return findViewById(id);
        } catch (Exception e) {
            return null;
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete account")
                .setMessage("Are you sure you want to permanently delete your account? This will remove your profile and all associated patients and surveys. This action cannot be undone.")
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // dismiss
                })
                .setPositiveButton("Delete", (dialog, which) -> performDeleteAccountRequest());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        // style buttons
        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (negative != null) {
            negative.setAllCaps(false);
            negative.setTypeface(null, Typeface.NORMAL);
            negative.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
        if (positive != null) {
            positive.setAllCaps(false);
            positive.setTypeface(null, Typeface.BOLD);
            int dangerColor;
            try {
                dangerColor = ContextCompat.getColor(this, R.color.red);
            } catch (Exception e) {
                dangerColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
            }
            positive.setTextColor(dangerColor);
        }
    }

    private void performDeleteAccountRequest() {
        // show progress
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        isRequestRunning = true;
        setAllClickable(false);

        String token = SharedPrefManager.getInstance(getApplicationContext()).getToken();
        if (token == null || token.trim().isEmpty()) {
            // Not authenticated locally - force local logout to keep UX consistent
            performImmediateLocalLogout("Not authenticated locally. Logging out.");
            return;
        }

        final String authHeader = buildAuthHeader(token.trim());
        System.out.println("DEBUG: delete-account authHeader = " + authHeader);

        Call<Void> call = apiService.deleteAccount(authHeader);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // normal success path
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                isRequestRunning = false;
                setAllClickable(true);

                int code = response.code();
                String respBody = "";
                try {
                    if (response.errorBody() != null) respBody = response.errorBody().string();
                    else if (response.body() != null) respBody = "body-not-null";
                } catch (Exception ignored) {}

                System.out.println("DEBUG: delete-account response code=" + code + " body=" + respBody);

                if (response.isSuccessful()) {
                    performImmediateLocalLogout("Account deleted successfully");
                } else {
                    if (code == 401 || code == 403) {
                        performImmediateLocalLogout("Session expired. Logged out.");
                    } else {
                        String message = "Failed to delete account: HTTP " + code;
                        if (!respBody.isEmpty()) message += " - " + respBody;
                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // handle protocol/okhttp errors that still mean success on server
                String err = t == null ? "unknown" : t.getMessage();
                System.out.println("DEBUG: delete-account failure: " + err);

                // hide progress and restore UI
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                isRequestRunning = false;
                setAllClickable(true);

                // If the server actually deleted the account but returned 204 with body,
                // OkHttp may raise an exception containing "204" and "non-zero Content-Length".
                // Treat that case as success.
                if (err != null && (
                        err.contains("HTTP 204") ||
                                err.contains("non-zero Content-Length") ||
                                err.contains("unexpected end of stream") ||
                                err.contains("unexpected end of stream") ||
                                err.contains("end of stream")
                )) {
                    // Treat as success — logout and go to login
                    performImmediateLocalLogout("Account deleted (server responded but client saw protocol warning).");
                    return;
                }

                // Otherwise show network error
                Toast.makeText(SettingsActivity.this, "Network error: " + err, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Clear local session and navigate to login (SecondActivity).
     * Uses finishAffinity() to remove all activities so user can't go back.
     */
    private void performImmediateLocalLogout(String toastMessage) {
        // Clear local storage / token
        SharedPrefManager.getInstance(getApplicationContext()).logout();

        // Debug: verify token cleared (remove prints in production)
        String nowToken = SharedPrefManager.getInstance(getApplicationContext()).getToken();
        System.out.println("DEBUG: token after logout = " + nowToken);

        // Start login activity and clear back stack
        Intent intent = new Intent(SettingsActivity.this, SecondActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Ensure all activities finished
        finishAffinity();

        // Inform user
        Toast.makeText(SettingsActivity.this, toastMessage, Toast.LENGTH_LONG).show();
    }

    private String buildAuthHeader(String token) {
        // If your SharedPrefManager returns token already prefixed (e.g. "Token abc...") return token;
        // If your server expects Bearer, return "Bearer " + token;
        return "Token " + token;
    }

    private void setAllClickable(boolean enabled) {
        if (btnChangeUsername != null) btnChangeUsername.setEnabled(enabled);
        if (btnChangePassword != null) btnChangePassword.setEnabled(enabled);
        if (btnRateUs != null) btnRateUs.setEnabled(enabled);
        if (btnLogout != null) btnLogout.setEnabled(enabled);
        if (btnDeleteAcc != null) btnDeleteAcc.setEnabled(enabled);
        if (btnBack != null) btnBack.setEnabled(enabled);
    }
}
