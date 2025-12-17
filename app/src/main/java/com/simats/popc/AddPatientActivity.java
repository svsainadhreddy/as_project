package com.simats.popc;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.simats.popc.model.PatientResponse;
import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPatientActivity extends AppCompatActivity {

    private TextView tvPatientId;
    private EditText etName, etPhone, etWeight, etHeight, etAgeInput;
    private TextView etBMI;
    private ImageView imgPatient, btnBack;
    private Button btnSave, btnNext;
    private TextView btnFemale, btnMale, btnOther, btnAgeMinus, btnAgePlus;

    private Uri selectedImage;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ApiService apiService;
    private String token;

    private String selectedGender = "Female";
    private boolean isSaved = false;
    private int savedPatientPk = -1;

    // NEW: pre-generated id fetched from server on open
    private String preGeneratedPatientId = null;

    enum NextAction { MANAGEMENT, DEMOGRAPHICS }
    private NextAction nextAction = NextAction.MANAGEMENT;

    // Colors
    private int colorSaveEnabled;
    private int colorSaveDisabled;
    private int colorNextEnabled;
    private int colorNextDisabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // UI bindings
        tvPatientId = findViewById(R.id.etPatientId);
        etName      = findViewById(R.id.etName);
        etPhone     = findViewById(R.id.etPhone);
        etWeight    = findViewById(R.id.etWeight);
        etHeight    = findViewById(R.id.etHeight);
        etBMI       = findViewById(R.id.etBMI);
        etAgeInput  = findViewById(R.id.etAgeInput);
        imgPatient  = findViewById(R.id.imgPatient);
        btnSave     = findViewById(R.id.btnSave);
        btnNext     = findViewById(R.id.btnNext);
        btnBack     = findViewById(R.id.btnBack);

        btnFemale = findViewById(R.id.btnFemale);
        btnMale   = findViewById(R.id.btnMale);
        btnOther  = findViewById(R.id.btnOther);

        btnAgeMinus = findViewById(R.id.btnAgeMinus);
        btnAgePlus  = findViewById(R.id.btnAgePlus);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        // Colors - adjust to match your drawable / theme if needed
        colorSaveEnabled = ContextCompat.getColor(this, R.color.icon_gray); // fallback
        // If you want exact hex used earlier:
        colorSaveEnabled = android.graphics.Color.parseColor("#9838D395"); // your previous tint
        colorSaveDisabled = android.graphics.Color.parseColor("#BDBDBD"); // grey disabled

        colorNextEnabled = android.graphics.Color.parseColor("#2E2D35"); // your previous next tint
        colorNextDisabled = android.graphics.Color.parseColor("#BDBDBD");

        setupFilters();
        setupImagePicker();
        setupListeners();

        // initial disabled state
        setButtonsEnabled(false);

        // NEW: fetch next patient id immediately and show in UI
        fetchAndShowNextPatientId();
    }

    private void setupFilters() {
        InputFilter nameFilter = (source, start, end, dest, dstart, dend) -> {
            StringBuilder filtered = new StringBuilder();
            for (int i = start; i < end; i++) {
                char ch = source.charAt(i);
                if (Character.isLetter(ch) || ch == ' ' || ch == '.') filtered.append(ch);
            }
            return filtered.length() == end - start ? null : filtered.toString();
        };
        etName.setFilters(new InputFilter[]{nameFilter, new InputFilter.LengthFilter(50)});

        InputFilter phoneFilter = (src, start, end, dest, dstart, dend) ->
                src.toString().matches("[0-9]*") ? null : "";
        etPhone.setFilters(new InputFilter[]{phoneFilter, new InputFilter.LengthFilter(10)});

        InputFilter decimalFilter = (src, start, end, dest, dstart, dend) ->
                src.toString().matches("[0-9.]*") ? null : "";
        etWeight.setFilters(new InputFilter[]{decimalFilter});
        etHeight.setFilters(new InputFilter[]{decimalFilter});
    }

    private void setupImagePicker() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImage = result.getData().getData();
                        Glide.with(this)
                                .load(selectedImage)
                                .circleCrop()
                                .into(imgPatient);
                        // image selection could make fields considered 'filled' — update buttons
                        updateButtonsState();
                    }
                });
        imgPatient.setOnClickListener(v -> openGallery());
    }

    private void setupListeners() {
        // watch inputs that are required
        TextWatcher requiredFieldsWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { updateButtonsState(); }
        };

        etName.addTextChangedListener(requiredFieldsWatcher);
        etPhone.addTextChangedListener(requiredFieldsWatcher);
        etWeight.addTextChangedListener(requiredFieldsWatcher);
        etHeight.addTextChangedListener(requiredFieldsWatcher);
        etAgeInput.addTextChangedListener(requiredFieldsWatcher);

        etWeight.addTextChangedListener(bmiWatcher);
        etHeight.addTextChangedListener(bmiWatcher);

        btnFemale.setOnClickListener(v -> { selectGender("Female"); updateButtonsState(); });
        btnMale.setOnClickListener(v -> { selectGender("Male"); updateButtonsState(); });
        btnOther.setOnClickListener(v -> { selectGender("Other"); updateButtonsState(); });

        btnAgeMinus.setOnClickListener(v -> { changeAge(-1); updateButtonsState(); });
        btnAgePlus.setOnClickListener(v -> { changeAge(1); updateButtonsState(); });

        btnSave.setOnClickListener(v -> {
            nextAction = NextAction.MANAGEMENT;
            saveOnly();
        });

        btnNext.setOnClickListener(v -> {
            nextAction = NextAction.DEMOGRAPHICS;
            saveOrNavigateNext();
        });

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, PatientManagementActivity.class));
            finish();
        });
    }

    // NEW: call backend to get next patient id and display
    private void fetchAndShowNextPatientId() {
        // This API must exist server-side: returns JSON { "patient_id": "PID0001" }
        apiService.getNextPatientId(token).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String pid = response.body().get("patient_id");
                    if (pid != null) {
                        preGeneratedPatientId = pid;
                        tvPatientId.setText(pid);
                    } else {
                        tvPatientId.setText("");
                    }
                } else {
                    tvPatientId.setText("");
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                tvPatientId.setText("");
            }
        });
    }

    private void selectGender(String gender) {
        selectedGender = gender;

        btnFemale.setBackgroundResource(android.R.color.transparent);
        btnMale.setBackgroundResource(android.R.color.transparent);
        btnOther.setBackgroundResource(android.R.color.transparent);

        btnFemale.setTextColor(ContextCompat.getColor(this, R.color.gray));
        btnMale.setTextColor(ContextCompat.getColor(this, R.color.gray));
        btnOther.setTextColor(ContextCompat.getColor(this, R.color.gray));

        if (gender.equals("Female")) {
            btnFemale.setBackgroundResource(R.drawable.bg_segment_selected);
            btnFemale.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else if (gender.equals("Male")) {
            btnMale.setBackgroundResource(R.drawable.bg_segment_selected);
            btnMale.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            btnOther.setBackgroundResource(R.drawable.bg_segment_selected);
            btnOther.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void changeAge(int delta) {
        try {
            int age = Integer.parseInt(etAgeInput.getText().toString());
            age += delta;
            if (age < 0) age = 0;
            etAgeInput.setText(String.valueOf(age));
        } catch (Exception e) {
            etAgeInput.setText("0");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(Intent.createChooser(intent, "Select Photo"));
    }

    private final TextWatcher bmiWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            calculateBMI();
            updateButtonsState();
        }
    };

    private void calculateBMI() {
        String w = etWeight.getText().toString().trim();
        String h = etHeight.getText().toString().trim();
        if (w.isEmpty() || h.isEmpty()) {
            etBMI.setText("");
            return;
        }
        try {
            float weight = Float.parseFloat(w);
            float height = Float.parseFloat(h);
            if (height == 0) { etBMI.setText(""); return; }
            float bmi = weight / ((height / 100f) * (height / 100f));
            etBMI.setText(String.format("%.2f", bmi));
        } catch (Exception ignored) {
            etBMI.setText("");
        }
    }

    // A validation used only for enabling buttons (no toast)
    private boolean requiredFieldsFilled() {
        // name, age, phone (10 digits), weight, height, bmi must be present
        String name = etName.getText().toString().trim();
        String age = etAgeInput.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        String bmi = etBMI.getText().toString().trim();

        if (name.isEmpty()) return false;
        if (age.isEmpty()) return false;
        if (phone.length() < 10) return false;
        if (weight.isEmpty()) return false;
        if (height.isEmpty()) return false;
        if (bmi.isEmpty()) return false;

        // gender is chosen by default, so it's fine
        return true;
    }

    // Update buttons enabled/disabled state and colors
    private void updateButtonsState() {
        boolean enabled = requiredFieldsFilled();
        setButtonsEnabled(enabled);
    }

    private void setButtonsEnabled(boolean enabled) {
        // Save button
        btnSave.setEnabled(enabled && !isSaved);
        btnSave.setAlpha((enabled && !isSaved) ? 1f : 0.6f);
        btnSave.setBackgroundTintList(ColorStateList.valueOf((enabled && !isSaved) ? colorSaveEnabled : colorSaveDisabled));

        // Next button
        btnNext.setEnabled(enabled);
        btnNext.setAlpha(enabled ? 1f : 0.6f);
        btnNext.setBackgroundTintList(ColorStateList.valueOf(enabled ? colorNextEnabled : colorNextDisabled));
    }

    private boolean validateInputs() {
        if (etName.getText().toString().trim().isEmpty()) return show("Enter Name");
        if (etAgeInput.getText().toString().trim().isEmpty()) return show("Enter Age");
        if (etPhone.getText().toString().trim().length() < 10) return show("Enter valid phone");
        if (etWeight.getText().toString().trim().isEmpty()) return show("Enter Weight");
        if (etHeight.getText().toString().trim().isEmpty()) return show("Enter Height");
        if (etBMI.getText().toString().trim().isEmpty()) return show("BMI not calculated");
        return true;
    }

    private boolean show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void saveOnly() {
        if (!validateInputs()) return;
        if (isSaved) { show("Already saved"); return; }
        performSave(new SaveCallback() {
            @Override public void onSaved(PatientResponse body) {
                isSaved = true;
                savedPatientPk = body.getId();
                if (body.getPatientId() != null) {
                    preGeneratedPatientId = body.getPatientId();
                    tvPatientId.setText(body.getPatientId());
                }
                btnSave.setEnabled(false);
                btnSave.setText("Saved");
                updateButtonsState();
            }
            @Override public void onFailed(String message) {
                show(message);
            }
        });
    }

    private void saveOrNavigateNext() {
        if (isSaved) {
            navigateToDemographics(savedPatientPk);
            return;
        }
        if (!validateInputs()) return;

        performSave(new SaveCallback() {
            @Override public void onSaved(PatientResponse body) {
                isSaved = true;
                savedPatientPk = body.getId();
                if (body.getPatientId() != null) {
                    preGeneratedPatientId = body.getPatientId();
                    tvPatientId.setText(body.getPatientId());
                }
                navigateToDemographics(savedPatientPk);
            }
            @Override public void onFailed(String message) {
                show(message);
            }
        });
    }

    private void performSave(SaveCallback cb) {
        RequestBody patientIdPart;
        if (preGeneratedPatientId != null && !preGeneratedPatientId.isEmpty()) {
            patientIdPart = RequestBody.create(preGeneratedPatientId, MediaType.parse("text/plain"));
        } else {
            // Send empty string (server will generate)
            patientIdPart = RequestBody.create("", MediaType.parse("text/plain"));
        }

        RequestBody name   = textPart(etName.getText().toString());
        RequestBody age    = textPart(etAgeInput.getText().toString());
        RequestBody phone  = textPart(etPhone.getText().toString());
        RequestBody weight = textPart(etWeight.getText().toString());
        RequestBody gender = textPart(selectedGender);
        RequestBody height = textPart(etHeight.getText().toString());
        RequestBody bmi    = textPart(etBMI.getText().toString());

        MultipartBody.Part photoPart = null;
        if (selectedImage != null) {
            File file = uriToFile(selectedImage);
            if (file != null) {
                RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
                photoPart = MultipartBody.Part.createFormData("photo", file.getName(), reqFile);
            }
        }

        // disable UI to avoid double submits
        setButtonsEnabled(false);

        apiService.createPatient(token, patientIdPart, name, age, phone, weight, gender, height, bmi, photoPart)
                .enqueue(new Callback<PatientResponse>() {
                    @Override
                    public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cb.onSaved(response.body());
                        } else {
                            // re-evaluate buttons and show message
                            updateButtonsState();
                            cb.onFailed("Failed to save patient");
                        }
                    }

                    @Override
                    public void onFailure(Call<PatientResponse> call, Throwable t) {
                        updateButtonsState();
                        cb.onFailed("Error: " + t.getMessage());
                    }
                });
    }

    private void navigateToDemographics(int id) {
        Intent i = new Intent(AddPatientActivity.this, PatientDemographicsActivity.class);
        i.putExtra("patient_id", id);
        startActivity(i);
        finish();
    }

    private RequestBody textPart(String v) {
        return RequestBody.create(v == null ? "" : v, MediaType.parse("text/plain"));
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            File temp = new File(getCacheDir(), "tmp.jpg");
            OutputStream out = new FileOutputStream(temp);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close();
            out.close();
            return temp;
        } catch (Exception e) { return null; }
    }

    private interface SaveCallback {
        void onSaved(PatientResponse body);
        void onFailed(String message);
    }
}



