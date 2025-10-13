package com.example.myapplicationpopc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplicationpopc.model.DoctorResponse;
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

public class ProfileActivity extends AppCompatActivity {

    EditText etName, etPhone, etEmail, etAgeInput;
    Spinner etSpecialization;
    TextView etDoctorId;
    ImageView ivProfile, btn1;
    Button btnUpdate;

    // Gender segmented control
    TextView btnFemale, btnMale, btnOther;

    // Age controls
    TextView btnAgeMinus, btnAgePlus;

    ApiService apiService;
    String token;
    Uri selectedImageUri;
    String selectedSpecialization = "Anesthesia";
    String selectedGender = "Female";
    int selectedAge = 24;

    ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Initialize Views
        etDoctorId = findViewById(R.id.etDoctorId);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etSpecialization = findViewById(R.id.etSpecialization);
        ivProfile = findViewById(R.id.ivProfile);
        btnUpdate = findViewById(R.id.btnUpdate);
        btn1 = findViewById(R.id.btnBack);
        btnFemale = findViewById(R.id.btnFemale);
        btnMale = findViewById(R.id.btnMale);
        btnOther = findViewById(R.id.btnOther);
        btnAgeMinus = findViewById(R.id.btnAgeMinus);
        btnAgePlus = findViewById(R.id.btnAgePlus);
        etAgeInput = findViewById(R.id.etAgeInput);

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.specialization_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etSpecialization.setAdapter(adapter);

        // Gender toggle
        btnFemale.setOnClickListener(v -> setGenderSelected("Female"));
        btnMale.setOnClickListener(v -> setGenderSelected("Male"));
        btnOther.setOnClickListener(v -> setGenderSelected("Other"));

        // --- Age logic ---
        etAgeInput.setText(String.valueOf(selectedAge));

        btnAgePlus.setOnClickListener(v -> {
            int age = getCurrentAge();
            if (age < 120) {
                age++;
                etAgeInput.setText(String.valueOf(age));
            }
        });

        btnAgeMinus.setOnClickListener(v -> {
            int age = getCurrentAge();
            if (age > 1) {
                age--;
                etAgeInput.setText(String.valueOf(age));
            }
        });

        // Update selectedAge when user types manually
        etAgeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    selectedAge = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    selectedAge = 0;
                }
            }
        });

        token = "Token " + SharedPrefManager.getInstance(this).getToken();
        apiService = ApiClient.getClient().create(ApiService.class);

        loadProfile();

        // Image picker
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivProfile.setImageURI(selectedImageUri);
                    }
                });

        ivProfile.setOnClickListener(v -> showImagePickerDialog());
        btnUpdate.setOnClickListener(v -> updateProfile());
        btn1.setOnClickListener(v -> {
            Intent i = new Intent(this, DoctorHomeActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });
    }

    private int getCurrentAge() {
        try {
            return Integer.parseInt(etAgeInput.getText().toString());
        } catch (NumberFormatException e) {
            return selectedAge;
        }
    }

    private void setGenderSelected(String gender) {
        selectedGender = gender;
        btnFemale.setBackgroundResource(
                gender.equals("Female") ? R.drawable.bg_segment_selected : android.R.color.transparent);
        btnMale.setBackgroundResource(
                gender.equals("Male") ? R.drawable.bg_segment_selected : android.R.color.transparent);
        btnOther.setBackgroundResource(
                gender.equals("Other") ? R.drawable.bg_segment_selected : android.R.color.transparent);

        btnFemale.setTextColor(gender.equals("Female") ? 0xFFFFFFFF : 0xFF555555);
        btnMale.setTextColor(gender.equals("Male") ? 0xFFFFFFFF : 0xFF555555);
        btnOther.setTextColor(gender.equals("Other") ? 0xFFFFFFFF : 0xFF555555);
    }

    private void showImagePickerDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Choose Option")
                .setItems(new CharSequence[]{"Gallery"}, (dialog, which) -> {
                    if (which == 0) openGallery();
                }).show();
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return;
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == 100 || requestCode == 101) &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Permission required to access gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfile() {
        apiService.getDoctorProfile(token).enqueue(new Callback<DoctorResponse>() {
            @Override
            public void onResponse(Call<DoctorResponse> call, Response<DoctorResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DoctorResponse doctor = response.body();
                    etDoctorId.setText(doctor.getDoctorId());
                    etName.setText(doctor.getName());
                    etPhone.setText(doctor.getPhone());
                    etEmail.setText(doctor.getEmail());

                    try {
                        selectedAge = Integer.parseInt(doctor.getAge());
                    } catch (Exception ignored) {}
                    etAgeInput.setText(String.valueOf(selectedAge));

                    if (doctor.getSpecialization() != null) {
                        int pos = ((ArrayAdapter) etSpecialization.getAdapter())
                                .getPosition(doctor.getSpecialization());
                        if (pos >= 0) etSpecialization.setSelection(pos);
                    }

                    if (doctor.getGender() != null) setGenderSelected(doctor.getGender());

                    if (doctor.getProfileImageUrl() != null) {
                        Glide.with(ProfileActivity.this)
                                .load(doctor.getProfileImageUrl())
                                .into(ivProfile);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DoctorResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = new File(getCacheDir(), "temp_profile_image.jpg");
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateProfile() {
        selectedAge = getCurrentAge();

        RequestBody name = RequestBody.create(etName.getText().toString(), MediaType.parse("text/plain"));
        RequestBody phone = RequestBody.create(etPhone.getText().toString(), MediaType.parse("text/plain"));
        RequestBody age = RequestBody.create(String.valueOf(selectedAge), MediaType.parse("text/plain"));
        RequestBody specialization = RequestBody.create(selectedSpecialization, MediaType.parse("text/plain"));
        RequestBody email = RequestBody.create(etEmail.getText().toString(), MediaType.parse("text/plain"));
        RequestBody gender = RequestBody.create(selectedGender, MediaType.parse("text/plain"));

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = uriToFile(selectedImageUri);
            if (file != null) {
                RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
                imagePart = MultipartBody.Part.createFormData("profile_image", file.getName(), reqFile);
            }
        }

        apiService.updateDoctorProfile(token, name, email, phone, age, gender, specialization, imagePart)
                .enqueue(new Callback<DoctorResponse>() {
                    @Override
                    public void onResponse(Call<DoctorResponse> call, Response<DoctorResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            loadProfile();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DoctorResponse> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
