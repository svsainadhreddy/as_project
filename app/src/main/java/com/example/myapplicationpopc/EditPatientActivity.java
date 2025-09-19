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

    ImageView imgPatient,btn1;
    EditText etPatientId, etName, etAge, etPhone, etWeight, etGender, etHeight;
    TextView etBMI;
    Button btnSave;
    Uri selectedImageUri = null;

    ApiService apiService;
    String token;
    int patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient);
        // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        imgPatient = findViewById(R.id.imgPatient);
        etPatientId = findViewById(R.id.etPatientId);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etWeight = findViewById(R.id.etWeight);
        etGender = findViewById(R.id.etGender);
        etHeight = findViewById(R.id.etHeight);
        etBMI = findViewById(R.id.etBMI);
        btnSave = findViewById(R.id.btnSave);
        btn1 = findViewById(R.id.btnBack);


        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();
        patientId = getIntent().getIntExtra("patient_id", -1);

        loadPatient(patientId);

        // Pick image
        imgPatient.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Calculate BMI dynamically
        TextWatcher bmiWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){}
            @Override
            public void afterTextChanged(Editable s){
                calculateBMI();
            }
        };
        etWeight.addTextChangedListener(bmiWatcher);
        etHeight.addTextChangedListener(bmiWatcher);

        btnSave.setOnClickListener(v -> savePatient());

        //back to EditPatientListActivity
        btn1.setOnClickListener(v -> {
            Intent i = new Intent(this, EditPatientListActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });
    }

    private void calculateBMI() {
        try {
            String weightStr = etWeight.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();

            if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                float weight = Float.parseFloat(weightStr);
                float heightCm = Float.parseFloat(heightStr);
                float heightM = heightCm / 100f;
                if (heightM > 0) {
                    float bmi = weight / (heightM * heightM);
                    etBMI.setText(String.format("%.2f", bmi));
                }
            } else {
                etBMI.setText("");
            }
        } catch (NumberFormatException e) {
            etBMI.setText("");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgPatient.setImageURI(selectedImageUri);
        }
    }

    private void loadPatient(int id) {
        apiService.getPatient(token, id).enqueue(new Callback<PatientResponse>() {
            @Override
            public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientResponse p = response.body();
                    etPatientId.setText(p.getPatientId());
                    etName.setText(p.getName());
                    etAge.setText(p.getAge());
                    etPhone.setText(p.getPhone());
                    etWeight.setText(p.getWeight());
                    etGender.setText(p.getGender());
                    etHeight.setText(p.getHeight());
                    etBMI.setText(p.getBmi());
                    if (p.getPhotoUrl() != null && !p.getPhotoUrl().isEmpty()) {
                        Glide.with(EditPatientActivity.this).load(ApiClient.BASE_URL + p.getPhotoUrl()).into(imgPatient);
                    }
                }
            }
            @Override public void onFailure(Call<PatientResponse> call, Throwable t) {
                Toast.makeText(EditPatientActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePatient() {
        if (patientId == -1) return;

        RequestBody patientIdBody = createPartFromString(etPatientId.getText().toString());
        RequestBody nameBody = createPartFromString(etName.getText().toString());
        RequestBody ageBody = createPartFromString(etAge.getText().toString());
        RequestBody phoneBody = createPartFromString(etPhone.getText().toString());
        RequestBody weightBody = createPartFromString(etWeight.getText().toString());
        RequestBody genderBody = createPartFromString(etGender.getText().toString());
        RequestBody heightBody = createPartFromString(etHeight.getText().toString());
        RequestBody bmiBody = createPartFromString(etBMI.getText().toString());

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            imagePart = MultipartBody.Part.createFormData("photo", file.getName(), reqFile);
        }

        apiService.updatePatient(token, patientId, patientIdBody, nameBody, ageBody, phoneBody,
                        weightBody, genderBody, heightBody, bmiBody, imagePart)
                .enqueue(new Callback<PatientResponse>() {
                    @Override
                    public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(EditPatientActivity.this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
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

    // Helper to create RequestBody from string
    private RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value != null ? value : "");
    }

}
