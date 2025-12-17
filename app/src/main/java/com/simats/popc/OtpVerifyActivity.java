package com.simats.popc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class OtpVerifyActivity extends AppCompatActivity {

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private EditText etOtpHidden;     // optional hidden field you had
    private EditText etOtpAutofill;   // small visible autofill target
    private Button btnVerify;
    private ImageButton ivBack;
    private TextView tvCaption;
    private View cardOtp;

    private String identifier;
    private ApiService api;

    // animations
    private Animation animCardEnter;
    private Animation animFieldPulse;
    private Animation animShake;
    private Animation animBtnSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        // bind views (IDs must match your XML)
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        etOtpHidden = findViewById(R.id.etOtp);          // optional hidden field
        etOtpAutofill = findViewById(R.id.etOtpAutofill);
        btnVerify = findViewById(R.id.btnVerify);
        ivBack = findViewById(R.id.ivBack);
        tvCaption = findViewById(R.id.tvCaption);
        cardOtp = findViewById(R.id.cardOtp);

        // load animations
        animCardEnter = AnimationUtils.loadAnimation(this, R.anim.card_enter);
        animFieldPulse = AnimationUtils.loadAnimation(this, R.anim.field_pulse);
        animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
        animBtnSuccess = AnimationUtils.loadAnimation(this, R.anim.button_success);

        // play card entrance
        if (cardOtp != null && animCardEnter != null) cardOtp.startAnimation(animCardEnter);

        // disable verify initially
        setVerifyEnabled(false);

        identifier = getIntent().getStringExtra("identifier");
        api = ApiClient.getClient().create(ApiService.class);

        // back
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // focus behavior: when clicking card, focus first empty field and show keyboard
        if (cardOtp != null) {
            cardOtp.setOnClickListener(v -> focusFirstEmpty());
        }

        // set up textwatchers and key listeners for each box
        setupOtpEditText(otp1, null, otp2);
        setupOtpEditText(otp2, otp1, otp3);
        setupOtpEditText(otp3, otp2, otp4);
        setupOtpEditText(otp4, otp3, otp5);
        setupOtpEditText(otp5, otp4, otp6);
        setupOtpEditText(otp6, otp5, null);

        // OTP field pulse on focus
        View.OnFocusChangeListener pulseOnFocus = (v, hasFocus) -> {
            if (hasFocus && animFieldPulse != null && cardOtp != null) {
                // small pulse of the card to indicate focus
                cardOtp.startAnimation(animFieldPulse);
            }
        };
        otp1.setOnFocusChangeListener(pulseOnFocus);
        otp2.setOnFocusChangeListener(pulseOnFocus);
        otp3.setOnFocusChangeListener(pulseOnFocus);
        otp4.setOnFocusChangeListener(pulseOnFocus);
        otp5.setOnFocusChangeListener(pulseOnFocus);
        otp6.setOnFocusChangeListener(pulseOnFocus);

        // autofill target watcher — system autofill or manual paste into this tiny field
        if (etOtpAutofill != null) {
            etOtpAutofill.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    String code = (s == null) ? "" : s.toString().replaceAll("\\D", "");
                    if (code.length() >= 6) {
                        // distribute first 6 digits
                        distributePaste(code.substring(0, 6), otp1);
                        // copy into hidden field if you need it
                        if (etOtpHidden != null) etOtpHidden.setText(code.substring(0, 6));
                        // clear/hide focus from autofill field
                        etOtpAutofill.clearFocus();
                        hideKeyboard(etOtpAutofill);
                        setVerifyEnabled(isAllDigitsEntered());
                    }
                }
            });
        }

        // verify click
        btnVerify.setOnClickListener(v -> {
            String otp = collectOtpFromFields();
            if (etOtpHidden != null) etOtpHidden.setText(otp);
            verifyOtpWithBackend(otp);
        });
    }

    /**
     * Set up each single-digit OTP EditText:
     * - Moves focus forward on input
     * - Moves focus back on delete (DEL)
     * - Handles paste (distributes digits)
     */
    private void setupOtpEditText(EditText current, EditText previous, EditText next) {
        if (current == null) return;

        current.addTextChangedListener(new TextWatcher() {
            private String previousText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousText = s.toString();
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                // If user pasted multiple characters into this box, distribute them
                if (text.length() > 1) {
                    distributePaste(text, current);
                    return;
                }

                // if user typed a single digit, move to next
                if (text.length() == 1) {
                    if (next != null) {
                        next.requestFocus();
                    } else {
                        // last field: hide keyboard
                        hideKeyboard(current);
                    }
                }

                // update verify button enable state
                setVerifyEnabled(isAllDigitsEntered());
            }
        });

        // handle DEL key to move focus back
        current.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (current.getText().toString().isEmpty() && previous != null) {
                    previous.requestFocus();
                    previous.setSelection(previous.getText().length());
                }
            }
            return false;
        });

        // when user taps the individual field, show keyboard
        current.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showKeyboard(current);
        });
    }

    /**
     * If a paste occurs (text length >1 in a single box or autofill target), distribute characters across fields
     */
    private void distributePaste(String pasted, EditText startField) {
        String digitsOnly = pasted.replaceAll("\\D", "");
        if (digitsOnly.isEmpty()) return;

        if (digitsOnly.length() > 6) digitsOnly = digitsOnly.substring(0, 6);

        char[] chars = digitsOnly.toCharArray();

        EditText[] fields = new EditText[]{otp1, otp2, otp3, otp4, otp5, otp6};
        int idx = 0;
        for (int i = 0; i < fields.length && idx < chars.length; i++) {
            fields[i].setText(String.valueOf(chars[idx]));
            idx++;
        }

        // move focus to next empty or hide keyboard if full
        for (EditText f : fields) {
            if (f.getText().toString().isEmpty()) {
                f.requestFocus();
                return;
            }
        }
        // all filled
        hideKeyboard(startField);
        setVerifyEnabled(true);
    }

    private void focusFirstEmpty() {
        if (otp1.getText().toString().isEmpty()) { otp1.requestFocus(); showKeyboard(otp1); return; }
        if (otp2.getText().toString().isEmpty()) { otp2.requestFocus(); showKeyboard(otp2); return; }
        if (otp3.getText().toString().isEmpty()) { otp3.requestFocus(); showKeyboard(otp3); return; }
        if (otp4.getText().toString().isEmpty()) { otp4.requestFocus(); showKeyboard(otp4); return; }
        if (otp5.getText().toString().isEmpty()) { otp5.requestFocus(); showKeyboard(otp5); return; }
        otp6.requestFocus(); showKeyboard(otp6);
    }

    private boolean isAllDigitsEntered() {
        return otp1.getText().toString().trim().length() == 1 &&
                otp2.getText().toString().trim().length() == 1 &&
                otp3.getText().toString().trim().length() == 1 &&
                otp4.getText().toString().trim().length() == 1 &&
                otp5.getText().toString().trim().length() == 1 &&
                otp6.getText().toString().trim().length() == 1;
    }

    private String collectOtpFromFields() {
        StringBuilder sb = new StringBuilder();
        sb.append(otp1.getText().toString().trim());
        sb.append(otp2.getText().toString().trim());
        sb.append(otp3.getText().toString().trim());
        sb.append(otp4.getText().toString().trim());
        sb.append(otp5.getText().toString().trim());
        sb.append(otp6.getText().toString().trim());
        return sb.toString();
    }

    private void setVerifyEnabled(boolean enabled) {
        btnVerify.setEnabled(enabled);
        if (enabled) {
            try {
                btnVerify.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_enabled_bg));
                btnVerify.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            } catch (Exception e) {
                // fallback keep existing
            }
        } else {
            try {
                btnVerify.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_disabled_bg));
                btnVerify.setTextColor(ContextCompat.getColor(this, R.color.btn_disabled_text));
            } catch (Exception e) {
                // fallback
            }
        }
    }

    private void verifyOtpWithBackend(String otp) {
        if (identifier == null || identifier.isEmpty()) {
            Toast.makeText(this, "Missing identifier.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (otp.length() != 6) {
            // shake to indicate problem
            if (cardOtp != null && animShake != null) cardOtp.startAnimation(animShake);
            Toast.makeText(this, "Please enter a 6-digit code.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerify.setEnabled(false);
        tvCaption.setText("Verifying...");

        Map<String, String> body = new HashMap<>();
        body.put("identifier", identifier);
        body.put("otp", otp);

        api.verifyOtp(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> c, Response<ResponseBody> resp) {
                btnVerify.setEnabled(true);
                setVerifyEnabled(isAllDigitsEntered());
                if (resp.isSuccessful()) {
                    // success animation on button then navigate
                    if (btnVerify != null && animBtnSuccess != null) {
                        btnVerify.startAnimation(animBtnSuccess);
                    }
                    btnVerify.postDelayed(() -> {
                        Intent i = new Intent(OtpVerifyActivity.this, ResetPasswordActivity.class);
                        i.putExtra("identifier", identifier);
                        i.putExtra("otp", otp);
                        startActivity(i);
                        finish();
                    }, 300);
                } else {
                    String message = "Invalid OTP!";
                    try {
                        if (resp.errorBody() != null) {
                            String err = resp.errorBody().string();
                            if (err != null && !err.isEmpty()) message = err;
                        }
                    } catch (IOException ignored) { }
                    // shake card on failure
                    if (cardOtp != null && animShake != null) cardOtp.startAnimation(animShake);
                    Toast.makeText(OtpVerifyActivity.this, message, Toast.LENGTH_LONG).show();
                    tvCaption.setText("Check your email or SMS for the code");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> c, Throwable t) {
                btnVerify.setEnabled(true);
                setVerifyEnabled(isAllDigitsEntered());
                String msg = t.getMessage() != null ? t.getMessage() : "Network error";
                if (cardOtp != null && animShake != null) cardOtp.startAnimation(animShake);
                Toast.makeText(OtpVerifyActivity.this, msg, Toast.LENGTH_LONG).show();
                tvCaption.setText("Check your email or SMS for the code");
            }
        });
    }

    private void showKeyboard(View v) {
        if (v == null) return;
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(View v) {
        if (v == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
