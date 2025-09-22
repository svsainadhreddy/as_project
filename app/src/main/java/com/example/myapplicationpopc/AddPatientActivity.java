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
    ImageView imgPatient,btn1;
    Button btnSave, btnNext;
    Uri selectedImage;
    ActivityResultLauncher<Intent> galleryLauncher;
    ApiService apiService;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        etPatientId = findViewById(R.id.etPatientId);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etWeight = findViewById(R.id.etWeight);
        etGender = findViewById(R.id.etGender);
        etHeight = findViewById(R.id.etHeight);
        etBMI = findViewById(R.id.etBMI);
        imgPatient = findViewById(R.id.imgPatient);
        btnSave = findViewById(R.id.btnSave);
        btnNext = findViewById(R.id.btnNext);
        // button
        btn1 = findViewById(R.id.btnBack);


        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        // Image picker setup
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImage = result.getData().getData();
                        imgPatient.setImageURI(selectedImage);
                    }
                });

        imgPatient.setOnClickListener(v -> openGallery());

        // Live BMI calculation
        etWeight.addTextChangedListener(bmiTextWatcher);
        etHeight.addTextChangedListener(bmiTextWatcher);

        btnSave.setOnClickListener(v -> savePatient());
        btnNext.setOnClickListener(v ->
                Toast.makeText(this, "Next clicked (implement assessment flow)", Toast.LENGTH_SHORT).show()
        );

        // back to PatientManagementActivity
        btn1.setOnClickListener(v -> {
            Intent i = new Intent(this, PatientManagementActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
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
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            calculateAndSetBMI();
        }
    };

    private void calculateAndSetBMI() {
        String weightStr = etWeight.getText().toString();
        String heightStr = etHeight.getText().toString();

        if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
            try {
                float weight = Float.parseFloat(weightStr);
                float height = Float.parseFloat(heightStr);
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
        RequestBody patientId = RequestBody.create(etPatientId.getText().toString(), MediaType.parse("text/plain"));
        RequestBody name = RequestBody.create(etName.getText().toString(), MediaType.parse("text/plain"));
        RequestBody age = RequestBody.create(etAge.getText().toString(), MediaType.parse("text/plain"));
        RequestBody phone = RequestBody.create(etPhone.getText().toString(), MediaType.parse("text/plain"));
        RequestBody weight = RequestBody.create(etWeight.getText().toString(), MediaType.parse("text/plain"));
        RequestBody gender = RequestBody.create(etGender.getText().toString(), MediaType.parse("text/plain"));
        RequestBody height = RequestBody.create(etHeight.getText().toString(), MediaType.parse("text/plain"));
        RequestBody bmi = RequestBody.create(etBMI.getText().toString(), MediaType.parse("text/plain"));

        MultipartBody.Part photoPart = null;
        if (selectedImage != null) {
            File file = uriToFile(selectedImage);
            if (file != null) {
                RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
                photoPart = MultipartBody.Part.createFormData("photo", file.getName(), reqFile);
            }
        }

        apiService.createPatient(token, patientId, name, age, phone, weight, gender, height, bmi, photoPart)
                .enqueue(new Callback<PatientResponse>() {
                    @Override
                    public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddPatientActivity.this, "Patient saved successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddPatientActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PatientResponse> call, Throwable t) {
                        Toast.makeText(AddPatientActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
