package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText etPass, etConfirm;
    private com.google.android.material.textfield.TextInputLayout layoutPass, layoutConfirm;
    private Button btnSubmit;
    private String identifier, otp;
    private ApiService api;
    private View cardContainer;
    private TextView tvHint;

    // Animations
    private Animation animCardEnter;
    private Animation animFieldPulse;
    private Animation animShake;
    private Animation animBtnSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // bind views
        etPass = findViewById(R.id.etNewPassword);
        etConfirm = findViewById(R.id.etConfirmPassword);
        // get parent TextInputLayout for pulse animation (wrap these in layout IDs in XML if needed)
        layoutPass = findViewById(R.id.labelNew) != null ? findViewById(R.id.labelNew).getParent() instanceof com.google.android.material.textfield.TextInputLayout ? (com.google.android.material.textfield.TextInputLayout) findViewById(R.id.labelNew).getParent() : null : null;
        // Not all layouts may have explicit ids for TextInputLayout; get by view hierarchy instead:
        layoutPass = findViewById(R.id.etNewPassword) != null && findViewById(R.id.etNewPassword).getParent() instanceof com.google.android.material.textfield.TextInputLayout
                ? (com.google.android.material.textfield.TextInputLayout) findViewById(R.id.etNewPassword).getParent()
                : null;
        layoutConfirm = findViewById(R.id.etConfirmPassword) != null && findViewById(R.id.etConfirmPassword).getParent() instanceof com.google.android.material.textfield.TextInputLayout
                ? (com.google.android.material.textfield.TextInputLayout) findViewById(R.id.etConfirmPassword).getParent()
                : null;

        btnSubmit = findViewById(R.id.btnSubmit);
        cardContainer = findViewById(R.id.card_container);
        tvHint = findViewById(R.id.tvHint);

        identifier = getIntent().getStringExtra("identifier");
        otp = getIntent().getStringExtra("otp");
        api = ApiClient.getClient().create(ApiService.class);

        // load animations
        animCardEnter = AnimationUtils.loadAnimation(this, R.anim.card_enter);
        animFieldPulse = AnimationUtils.loadAnimation(this, R.anim.field_pulse);
        animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
        animBtnSuccess = AnimationUtils.loadAnimation(this, R.anim.button_success);

        // start card entrance animation
        if (cardContainer != null) cardContainer.startAnimation(animCardEnter);

        // focus listeners to pulse the field container when focused
        if (etPass != null) {
            etPass.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && layoutPass != null) layoutPass.startAnimation(animFieldPulse);
            });
        }
        if (etConfirm != null) {
            etConfirm.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && layoutConfirm != null) layoutConfirm.startAnimation(animFieldPulse);
            });
        }

        btnSubmit.setOnClickListener(v -> submitPassword());
    }

    private void submitPassword() {
        String p = etPass.getText() != null ? etPass.getText().toString().trim() : "";
        String c = etConfirm.getText() != null ? etConfirm.getText().toString().trim() : "";

        // Basic client-side validation
        if (p.isEmpty() || c.isEmpty()) {
            showError("Please fill both password fields");
            if (layoutPass != null) layoutPass.startAnimation(animShake);
            if (layoutConfirm != null) layoutConfirm.startAnimation(animShake);
            return;
        }

        if (!p.equals(c)) {
            showError("Passwords don't match");
            // shake confirm field for emphasis
            if (layoutConfirm != null) layoutConfirm.startAnimation(animShake);
            // optional: set error text on TextInputLayout
            if (layoutConfirm != null) layoutConfirm.setError("Passwords do not match");
            return;
        } else {
            if (layoutConfirm != null) layoutConfirm.setError(null);
        }

        // optional: check strength (at least 8 chars, mixed case, numbers)
        if (!isStrongPassword(p)) {
            showError("Password must be at least 8 characters with mixed case and numbers");
            if (layoutPass != null) layoutPass.startAnimation(animShake);
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("identifier", identifier);
        body.put("otp", otp);
        body.put("new_password", p);

        // disable button while processing
        btnSubmit.setEnabled(false);
        btnSubmit.setAlpha(0.6f);
        tvHint.setText("Resetting password...");

        api.resetPassword(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                btnSubmit.setEnabled(true);
                btnSubmit.setAlpha(1f);
                if (resp.isSuccessful()) {
                    // animate button success then navigate
                    btnSubmit.startAnimation(animBtnSuccess);
                    // small delay to let animation show
                    btnSubmit.postDelayed(() -> {
                        Intent intent = new Intent(ResetPasswordActivity.this, SecondActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }, 320);
                } else {
                    String message = "Error resetting password";
                    try {
                        if (resp.errorBody() != null) {
                            String err = resp.errorBody().string();
                            if (err != null && !err.isEmpty()) message = err;
                        }
                    } catch (IOException ignored) {}
                    showError(message);
                    // shake card to indicate failure
                    if (cardContainer != null) cardContainer.startAnimation(animShake);
                    tvHint.setText("Check your connection and try again");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setAlpha(1f);
                showError("Network error: " + (t.getMessage() != null ? t.getMessage() : "unknown"));
                if (cardContainer != null) cardContainer.startAnimation(animShake);
                tvHint.setText("Check your connection and try again");
            }
        });
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // basic password strength validator
    private boolean isStrongPassword(String p) {
        if (p.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char ch : p.toCharArray()) {
            if (Character.isUpperCase(ch)) hasUpper = true;
            if (Character.isLowerCase(ch)) hasLower = true;
            if (Character.isDigit(ch)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}
