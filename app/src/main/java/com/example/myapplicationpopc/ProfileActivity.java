package com.example.myapplicationpopc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

    EditText etName, etPhone, etEmail, etAge, etDoctor_gender;
    Spinner etSpecialization;
    TextView etDoctorId;
    ImageView ivProfile, btn1;
    Button btnUpdate;

    ApiService apiService;
    String token;
    Uri selectedImageUri;
    String selectedSpecialization = "Anesthesia";

    ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Bind UI
        etDoctorId = findViewById(R.id.etDoctorId);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etSpecialization = findViewById(R.id.etSpecialization);
        etAge = findViewById(R.id.etAge);
        etDoctor_gender = findViewById(R.id.etDoctor_gender);
        ivProfile = findViewById(R.id.ivProfile);
        btnUpdate = findViewById(R.id.btnUpdate);
        btn1 = findViewById(R.id.btnBack);

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.specialization_array, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etSpecialization.setAdapter(adapter);

        etSpecialization.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedSpecialization = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSpecialization = "Anesthesia";
            }
        });

        token = "Token " + SharedPrefManager.getInstance(this).getToken();
        apiService = ApiClient.getClient().create(ApiService.class);

        loadProfile();

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivProfile.setImageURI(selectedImageUri);
                    }
                }
        );

        ivProfile.setOnClickListener(v -> showImagePickerDialog());
        btnUpdate.setOnClickListener(v -> updateProfile());

        // Back to home
        btn1.setOnClickListener(v -> {
            Intent i = new Intent(this, DoctorHomeActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });
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
                    etAge.setText(doctor.getAge());
                    etDoctor_gender.setText(doctor.getGender());

                    // Set specialization to dropdown
                    if (doctor.getSpecialization() != null) {
                        int pos = ((ArrayAdapter) etSpecialization.getAdapter())
                                .getPosition(doctor.getSpecialization());
                        if (pos >= 0) etSpecialization.setSelection(pos);
                    }

                    if (doctor.getProfileImageUrl() != null) {
                        Glide.with(ProfileActivity.this)
                                .load(ApiClient.BASE_URL + doctor.getProfileImageUrl())
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
        RequestBody name = RequestBody.create(etName.getText().toString(), MediaType.parse("text/plain"));
        RequestBody phone = RequestBody.create(etPhone.getText().toString(), MediaType.parse("text/plain"));
        RequestBody age = RequestBody.create(etAge.getText().toString(), MediaType.parse("text/plain"));
        RequestBody gender = RequestBody.create(etDoctor_gender.getText().toString(), MediaType.parse("text/plain"));
        RequestBody specialization = RequestBody.create(selectedSpecialization, MediaType.parse("text/plain"));
        RequestBody email = RequestBody.create(etEmail.getText().toString(), MediaType.parse("text/plain"));

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
