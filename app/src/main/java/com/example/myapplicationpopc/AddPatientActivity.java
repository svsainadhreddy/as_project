package com.example.myapplicationpopc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.Spanned;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

    EditText etPatientId, etName, etAge, etPhone, etWeight, etHeight, etBMI;
    Spinner spGender;
    ImageView imgPatient, btn1;
    Button btnSave, btnNext;
    Uri selectedImage;
    ActivityResultLauncher<Intent> galleryLauncher;
    ApiService apiService;
    String token;

    enum NextAction { MANAGEMENT, DEMOGRAPHICS }
    private NextAction nextAction = NextAction.MANAGEMENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // UI elements
        etPatientId = findViewById(R.id.etPatientId);
        etName      = findViewById(R.id.etName);
        etAge       = findViewById(R.id.etAge);
        etPhone     = findViewById(R.id.etPhone);
        etWeight    = findViewById(R.id.etWeight);
        etHeight    = findViewById(R.id.etHeight);
        etBMI       = findViewById(R.id.etBMI);
        spGender    = findViewById(R.id.spGender);
        imgPatient  = findViewById(R.id.imgPatient);
        btnSave     = findViewById(R.id.btnSave);
        btnNext     = findViewById(R.id.btnNext);
        btn1        = findViewById(R.id.btnBack);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        // ✅ Name field restriction: alphabets, dot, space
        InputFilter nameFilter = new InputFilter() {
            public CharSequence filter(CharSequence src, int start, int end, Spanned dest, int dstart, int dend) {
                if (src.toString().matches("[a-zA-Z. ]*")) {
                    return null;
                }
                Toast.makeText(AddPatientActivity.this, "Only alphabets, dot, and space allowed", Toast.LENGTH_SHORT).show();
                return "";
            }
        };
        etName.setFilters(new InputFilter[]{nameFilter});

        // ✅ Setup gender dropdown
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Select Gender", "Male", "Female", "Other"}
        );
        spGender.setAdapter(genderAdapter);

        // ✅ Modern photo picker
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImage = result.getData().getData();
                        Glide.with(this)
                                .load(selectedImage)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person_outline)
                                .error(R.drawable.ic_person_outline)
                                .into(imgPatient);
                    }
                });

        imgPatient.setOnClickListener(v -> openGallery());

        etWeight.addTextChangedListener(bmiTextWatcher);
        etHeight.addTextChangedListener(bmiTextWatcher);

        btnSave.setOnClickListener(v -> {
            nextAction = NextAction.MANAGEMENT;
            validateAndSave();
        });

        btnNext.setOnClickListener(v -> {
            nextAction = NextAction.DEMOGRAPHICS;
            validateAndSave();
        });

        btn1.setOnClickListener(v -> {
            startActivity(new Intent(this, PatientManagementActivity.class));
            finish();
        });
    }

    private void openGallery() {
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

    private void validateAndSave() {
        if (etPatientId.getText().toString().trim().isEmpty()) {
            etPatientId.requestFocus();
            showToast("Enter Patient ID");
            return;
        }
        if (etName.getText().toString().trim().isEmpty()) {
            etName.requestFocus();
            showToast("Enter Name");
            return;
        }
        if (etAge.getText().toString().trim().isEmpty()) {
            etAge.requestFocus();
            showToast("Enter Age");
            return;
        }
        if (etPhone.getText().toString().trim().isEmpty()) {
            etPhone.requestFocus();
            showToast("Enter Phone Number");
            return;
        }
        if (etWeight.getText().toString().trim().isEmpty()) {
            etWeight.requestFocus();
            showToast("Enter Weight");
            return;
        }
        if (spGender.getSelectedItemPosition() == 0) {
            spGender.requestFocus();
            showToast("Select Gender");
            return;
        }
        if (etHeight.getText().toString().trim().isEmpty()) {
            etHeight.requestFocus();
            showToast("Enter Height");
            return;
        }
        if (etBMI.getText().toString().trim().isEmpty()) {
            etBMI.requestFocus();
            showToast("Enter BMI");
            return;
        }

        savePatient();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void savePatient() {
        RequestBody patientId = textPart(etPatientId.getText().toString());
        RequestBody name      = textPart(etName.getText().toString());
        RequestBody age       = textPart(etAge.getText().toString());
        RequestBody phone     = textPart(etPhone.getText().toString());
        RequestBody weight    = textPart(etWeight.getText().toString());
        RequestBody gender    = textPart(spGender.getSelectedItem().toString());
        RequestBody height    = textPart(etHeight.getText().toString());
        RequestBody bmi       = textPart(etBMI.getText().toString());

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
                            showToast("Patient saved successfully");

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
                            showToast("Patient Id already exists");
                        }
                    }

                    @Override
                    public void onFailure(Call<PatientResponse> call, Throwable t) {
                        showToast("Error: " + t.getMessage());
                    }
                });
    }

    private RequestBody textPart(String value) {
        return RequestBody.create(value.trim(), MediaType.parse("text/plain"));
    }
}
