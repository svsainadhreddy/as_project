package com.example.myapplicationpopc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.model.PatientResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPatientActivity extends AppCompatActivity {

    EditText etPatientId, etName, etAge, etPhone, etWeight, etGender, etHeight;
    TextView etBMI;
    ImageView imgPatient, btn1;
    Button btnSave, btnNext;
    Uri selectedImage;
    ActivityResultLauncher<Intent> galleryLauncher;
    ApiService apiService;
    String token;

    // ✅ to know where to go after saving
    enum NextAction { MANAGEMENT, DEMOGRAPHICS }
    private NextAction nextAction = NextAction.MANAGEMENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etPatientId = findViewById(R.id.etPatientId);
        etName      = findViewById(R.id.etName);
        etAge       = findViewById(R.id.etAge);
        etPhone     = findViewById(R.id.etPhone);
        etWeight    = findViewById(R.id.etWeight);
        etGender    = findViewById(R.id.etGender);
        etHeight    = findViewById(R.id.etHeight);
        etBMI       = findViewById(R.id.etBMI);
        imgPatient  = findViewById(R.id.imgPatient);
        btnSave     = findViewById(R.id.btnSave);
        btnNext     = findViewById(R.id.btnNext);
        btn1        = findViewById(R.id.btnBack);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        // ✅ modern picker works on all Android versions
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImage = result.getData().getData();
                        imgPatient.setImageURI(selectedImage);
                    }
                });

        imgPatient.setOnClickListener(v -> openGallery());

        etWeight.addTextChangedListener(bmiTextWatcher);
        etHeight.addTextChangedListener(bmiTextWatcher);

        btnSave.setOnClickListener(v -> {
            nextAction = NextAction.MANAGEMENT;
            savePatient();
        });

        btnNext.setOnClickListener(v -> {
            nextAction = NextAction.DEMOGRAPHICS;
            savePatient();
        });

        btn1.setOnClickListener(v -> {
            startActivity(new Intent(this, PatientManagementActivity.class));
            finish();
        });
    }

    private void openGallery() {
        // ✅ safer universal photo picker
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryLauncher.launch(Intent.createChooser(intent, "Select Photo"));
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File temp = new File(getCacheDir(), "tmp_image.jpg");
            OutputStream out = new FileOutputStream(temp);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) out.write(buf, 0, len);
            out.close();
            inputStream.close();
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private final TextWatcher bmiTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(Editable s) { calculateAndSetBMI(); }
    };

    private void calculateAndSetBMI() {
        String w = etWeight.getText().toString();
        String h = etHeight.getText().toString();
        if (!w.isEmpty() && !h.isEmpty()) {
            try {
                float weight = Float.parseFloat(w);
                float height = Float.parseFloat(h);
                if (height > 0) {
                    float bmiValue = weight / ((height / 100) * (height / 100));
                    etBMI.setText(String.format("%.2f", bmiValue));
                }
            } catch (NumberFormatException e) {
                etBMI.setText("");
            }
        } else {
            etBMI.setText("");
        }
    }

    private void savePatient() {
        // basic validation
        if (etPatientId.getText().toString().trim().isEmpty() ||
                etName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Patient ID and Name are required", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody patientId = textPart(etPatientId.getText().toString());
        RequestBody name      = textPart(etName.getText().toString());
        RequestBody age       = textPartOrEmpty(etAge.getText().toString());
        RequestBody phone     = textPartOrEmpty(etPhone.getText().toString());
        RequestBody weight    = textPartOrEmpty(etWeight.getText().toString());
        RequestBody gender    = textPartOrEmpty(etGender.getText().toString());
        RequestBody height    = textPartOrEmpty(etHeight.getText().toString());
        RequestBody bmi       = textPartOrEmpty(etBMI.getText().toString());

        MultipartBody.Part photoPart = null;
        if (selectedImage != null) {
            File file = uriToFile(selectedImage);
            if (file != null) {
                RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
                photoPart = MultipartBody.Part.createFormData("photo", file.getName(), reqFile);
            }
        }

        apiService.createPatient(token, patientId, name, age, phone,
                        weight, gender, height, bmi, photoPart)
                .enqueue(new Callback<PatientResponse>() {
                    @Override
                    public void onResponse(Call<PatientResponse> call,
                                           Response<PatientResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            int createdId = response.body().getId();
                            Toast.makeText(AddPatientActivity.this,
                                    "Patient saved successfully",
                                    Toast.LENGTH_SHORT).show();

                            if (nextAction == NextAction.MANAGEMENT) {
                                startActivity(new Intent(AddPatientActivity.this,
                                        PatientManagementActivity.class));
                            } else {
                                Intent i = new Intent(AddPatientActivity.this,
                                        PatientDemographicsActivity.class);
                                i.putExtra("patient_id", createdId);
                                startActivity(i);
                            }
                            finish();
                        } else {
                            Toast.makeText(AddPatientActivity.this,
                                    "Failed: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PatientResponse> call, Throwable t) {
                        Toast.makeText(AddPatientActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------- helpers ----------
    private RequestBody textPart(String value) {
        return RequestBody.create(value.trim(), MediaType.parse("text/plain"));
    }

    // ✅ always send empty string if no value (prevents 400 Bad Request)
    private RequestBody textPartOrEmpty(String value) {
        return RequestBody.create(
                value == null ? "" : value.trim(),
                MediaType.parse("text/plain")
        );
    }
}
