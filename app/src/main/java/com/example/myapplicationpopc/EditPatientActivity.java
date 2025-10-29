package com.example.myapplicationpopc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.myapplicationpopc.model.PatientResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPatientActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final String TAG = "EDIT_PATIENT";

    private ImageView imgPatient, btnBack;
    private EditText etPatientId, etName, etPhone, etWeight, etHeight, etAgeInput;
    private TextView etBMI;
    private Button btnSave;
    private Uri selectedImageUri = null;

    private TextView btnFemale, btnMale, btnOther;
    private String selectedGender = "";

    private TextView btnAgeMinus, btnAgePlus;

    private ApiService apiService;
    private String token;
    private int patientId;
    private String originalPatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        imgPatient = findViewById(R.id.imgPatient);
        btnBack = findViewById(R.id.btnBack);
        etPatientId = findViewById(R.id.etPatientId);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etBMI = findViewById(R.id.etBMI);
        btnSave = findViewById(R.id.btnSave);
        btnFemale = findViewById(R.id.btnFemale);
        btnMale = findViewById(R.id.btnMale);
        btnOther = findViewById(R.id.btnOther);
        btnAgeMinus = findViewById(R.id.btnAgeMinus);
        btnAgePlus = findViewById(R.id.btnAgePlus);
        etAgeInput = findViewById(R.id.etAgeInput);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();
        patientId = getIntent().getIntExtra("patient_id", -1);

        if (patientId != -1) loadPatient(patientId);
        else Toast.makeText(this, "Invalid Patient ID", Toast.LENGTH_SHORT).show();

        imgPatient.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // ✅ Restrict name input: alphabets and spaces only
        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String name = s.toString();
                if (!name.matches("^[a-zA-Z ]*$")) {
                    etName.setError("Only alphabets allowed");
                }
            }
        });

        // ✅ Phone validation live feedback
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 10) {
                    etPhone.setError("Maximum 10 digits allowed");
                } else if (s.length() < 10 && s.length() > 0) {
                    etPhone.setError("Digits entered: " + s.length() + "/10");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Gender selection
        View.OnClickListener genderClickListener = v -> {
            clearGenderSelection();
            ((TextView) v).setBackgroundResource(R.drawable.bg_segment_selected);
            ((TextView) v).setTextColor(getResources().getColor(android.R.color.white));
            if (v == btnFemale) selectedGender = "Female";
            else if (v == btnMale) selectedGender = "Male";
            else selectedGender = "Other";
        };
        btnFemale.setOnClickListener(genderClickListener);
        btnMale.setOnClickListener(genderClickListener);
        btnOther.setOnClickListener(genderClickListener);

        // Age control
        btnAgeMinus.setOnClickListener(v -> {
            int age = getAgeValue();
            if (age > 1) etAgeInput.setText(String.valueOf(age - 1));
        });
        btnAgePlus.setOnClickListener(v -> {
            int age = getAgeValue();
            etAgeInput.setText(String.valueOf(age + 1));
        });

        // Dynamic BMI
        TextWatcher bmiWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { calculateBMI(); }
        };
        etWeight.addTextChangedListener(bmiWatcher);
        etHeight.addTextChangedListener(bmiWatcher);

        btnSave.setOnClickListener(v -> savePatient());
        btnBack.setOnClickListener(v -> finish());
    }

    private void clearGenderSelection() {
        btnFemale.setBackgroundResource(android.R.color.transparent);
        btnMale.setBackgroundResource(android.R.color.transparent);
        btnOther.setBackgroundResource(android.R.color.transparent);
        int gray = getResources().getColor(R.color.gray);
        btnFemale.setTextColor(gray);
        btnMale.setTextColor(gray);
        btnOther.setTextColor(gray);
    }

    private int getAgeValue() {
        try {
            String str = etAgeInput.getText().toString().trim();
            return str.isEmpty() ? 24 : Math.max(1, Integer.parseInt(str));
        } catch (Exception e) { return 24; }
    }

    private void calculateBMI() {
        try {
            String w = etWeight.getText().toString().trim();
            String h = etHeight.getText().toString().trim();
            if (!w.isEmpty() && !h.isEmpty()) {
                float weight = Float.parseFloat(w);
                float heightM = Float.parseFloat(h) / 100f;
                if (heightM > 0) {
                    float bmi = weight / (heightM * heightM);
                    etBMI.setText(String.format("%.2f", bmi));
                }
            } else etBMI.setText("");
        } catch (NumberFormatException e) {
            etBMI.setText("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).circleCrop().into(imgPatient);
        }
    }

    private void loadPatient(int id) {
        apiService.getPatient(token, id).enqueue(new Callback<PatientResponse>() {
            @Override
            public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientResponse p = response.body();
                    originalPatientId = p.getPatientId();
                    etPatientId.setText(originalPatientId);
                    etName.setText(p.getName());
                    etPhone.setText(p.getPhone());
                    etWeight.setText(p.getWeight());
                    etHeight.setText(p.getHeight());
                    etBMI.setText(p.getBmi());
                    etAgeInput.setText(p.getAge());

                    if (p.getGender() != null) {
                        selectedGender = p.getGender();
                        clearGenderSelection();
                        if ("Female".equalsIgnoreCase(selectedGender)) {
                            btnFemale.setBackgroundResource(R.drawable.bg_segment_selected);
                            btnFemale.setTextColor(getResources().getColor(android.R.color.white));
                        } else if ("Male".equalsIgnoreCase(selectedGender)) {
                            btnMale.setBackgroundResource(R.drawable.bg_segment_selected);
                            btnMale.setTextColor(getResources().getColor(android.R.color.white));
                        } else {
                            btnOther.setBackgroundResource(R.drawable.bg_segment_selected);
                            btnOther.setTextColor(getResources().getColor(android.R.color.white));
                        }
                    }

                    if (p.getPhotoUrl() != null && !p.getPhotoUrl().isEmpty()) {
                        Glide.with(EditPatientActivity.this)
                                .load(p.getPhotoUrl())
                                .circleCrop()
                                .into(imgPatient);
                    }
                } else {
                    Toast.makeText(EditPatientActivity.this, "Failed to load patient", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PatientResponse> call, Throwable t) {
                Toast.makeText(EditPatientActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePatient() {
        if (patientId == -1) return;

        // ✅ Validation before saving
        String nameStr = etName.getText().toString().trim();
        String phoneStr = etPhone.getText().toString().trim();

        if (nameStr.isEmpty()) {
            etName.setError("Name required");
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!nameStr.matches("^[a-zA-Z ]+$")) {
            etName.setError("Only alphabets allowed");
            Toast.makeText(this, "Name should contain only alphabets", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phoneStr.isEmpty()) {
            etPhone.setError("Phone number required");
            Toast.makeText(this, "Phone number required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phoneStr.matches("\\d{10}")) {
            etPhone.setError("Digits entered: " + phoneStr.length() + "/10");
            Toast.makeText(this, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            imagePart = MultipartBody.Part.createFormData("photo", file.getName(), reqFile);
        }

        String enteredPatientId = etPatientId.getText().toString().trim();
        RequestBody patientIdBody = null;
        if (!enteredPatientId.isEmpty() && !enteredPatientId.equals(originalPatientId)) {
            patientIdBody = createPartFromString(enteredPatientId);
        }

        RequestBody nameBody = createPartFromString(nameStr);
        RequestBody ageBody = createPartFromString(etAgeInput.getText().toString());
        RequestBody phoneBody = createPartFromString(phoneStr);
        RequestBody weightBody = createPartFromString(etWeight.getText().toString());
        RequestBody genderBody = createPartFromString(selectedGender);
        RequestBody heightBody = createPartFromString(etHeight.getText().toString());
        RequestBody bmiBody = createPartFromString(etBMI.getText().toString());

        apiService.updatePatient(token, patientId,
                        patientIdBody, nameBody, ageBody, phoneBody,
                        weightBody, genderBody, heightBody, bmiBody, imagePart)
                .enqueue(new Callback<PatientResponse>() {
                    @Override
                    public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(EditPatientActivity.this, "✅ Patient updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (response.code() == 400) {
                            Toast.makeText(EditPatientActivity.this, "⚠️ Patient ID already exists!", Toast.LENGTH_LONG).show();
                            etPatientId.setError("This ID is already used");
                        } else {
                            Toast.makeText(EditPatientActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PatientResponse> call, Throwable t) {
                        Toast.makeText(EditPatientActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private RequestBody createPartFromString(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}
