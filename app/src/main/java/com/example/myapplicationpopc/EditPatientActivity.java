package com.example.myapplicationpopc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private ImageView imgPatient, btnBack;
    private EditText etPatientId, etName, etAge, etPhone, etWeight, etGender, etHeight;
    private TextView etBMI;
    private Button btnSave;
    private Uri selectedImageUri = null;

    private ApiService apiService;
    private String token;
    private int patientId;
    private String originalPatientId; // To track the existing patient ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        imgPatient = findViewById(R.id.imgPatient);
        btnBack = findViewById(R.id.btnBack);
        etPatientId = findViewById(R.id.etPatientId);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etWeight = findViewById(R.id.etWeight);
        etGender = findViewById(R.id.etGender);
        etHeight = findViewById(R.id.etHeight);
        etBMI = findViewById(R.id.etBMI);
        btnSave = findViewById(R.id.btnSave);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();
        patientId = getIntent().getIntExtra("patient_id", -1);

        if (patientId != -1) loadPatient(patientId);
        else Toast.makeText(this, "Invalid Patient ID", Toast.LENGTH_SHORT).show();

        // Pick image
        imgPatient.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Dynamic BMI calculation
        TextWatcher bmiWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { calculateBMI(); }
        };
        etWeight.addTextChangedListener(bmiWatcher);
        etHeight.addTextChangedListener(bmiWatcher);

        btnSave.setOnClickListener(v -> savePatient());
        btnBack.setOnClickListener(v -> finish());
    }

    private void calculateBMI() {
        try {
            String weightStr = etWeight.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();
            if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                float weight = Float.parseFloat(weightStr);
                float heightM = Float.parseFloat(heightStr) / 100f;
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
            Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(imgPatient);
        }
    }

    private void loadPatient(int id) {
        apiService.getPatient(token, id).enqueue(new Callback<PatientResponse>() {
            @Override
            public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientResponse p = response.body();
                    originalPatientId = p.getPatientId(); // store original ID
                    etPatientId.setText(originalPatientId);
                    etName.setText(p.getName());
                    etAge.setText(p.getAge());
                    etPhone.setText(p.getPhone());
                    etWeight.setText(p.getWeight());
                    etGender.setText(p.getGender());
                    etHeight.setText(p.getHeight());
                    etBMI.setText(p.getBmi());

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

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            imagePart = MultipartBody.Part.createFormData("photo", file.getName(), reqFile);
        }

        // Only send patient_id if changed
        String enteredPatientId = etPatientId.getText().toString().trim();
        RequestBody patientIdBody = null;
        if (!enteredPatientId.isEmpty() && !enteredPatientId.equals(originalPatientId)) {
            patientIdBody = createPartFromString(enteredPatientId);
        }

        RequestBody nameBody = etName.getText().toString().isEmpty() ? null : createPartFromString(etName.getText().toString());
        RequestBody ageBody = etAge.getText().toString().isEmpty() ? null : createPartFromString(etAge.getText().toString());
        RequestBody phoneBody = etPhone.getText().toString().isEmpty() ? null : createPartFromString(etPhone.getText().toString());
        RequestBody weightBody = etWeight.getText().toString().isEmpty() ? null : createPartFromString(etWeight.getText().toString());
        RequestBody genderBody = etGender.getText().toString().isEmpty() ? null : createPartFromString(etGender.getText().toString());
        RequestBody heightBody = etHeight.getText().toString().isEmpty() ? null : createPartFromString(etHeight.getText().toString());
        RequestBody bmiBody = etBMI.getText().toString().isEmpty() ? null : createPartFromString(etBMI.getText().toString());

        apiService.updatePatient(token, patientId,
                        patientIdBody, nameBody, ageBody, phoneBody,
                        weightBody, genderBody, heightBody, bmiBody, imagePart)
                .enqueue(new Callback<PatientResponse>() {
                    @Override
                    public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(EditPatientActivity.this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (response.code() == 400) {
                            Toast.makeText(EditPatientActivity.this, "Patient ID already exists", Toast.LENGTH_SHORT).show();
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
        return RequestBody.create(MediaType.parse("text/plain"), value != null ? value : "");
    }
}
