package com.example.myapplicationpopc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplicationpopc.model.DoctorResponse;
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


public class ProfileActivity extends AppCompatActivity {
        EditText etDoctorId, etName, etPhone, etEmail, etSpecialization, etAge,etDoctor_gender;
        ImageView ivProfile;
        ApiService apiService;
        String token;
        Uri selectedImageUri;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile);

            etDoctorId = findViewById(R.id.etDoctorId);
            etName = findViewById(R.id.etName);
            etPhone = findViewById(R.id.etPhone);
            etEmail = findViewById(R.id.etEmail);
            etSpecialization = findViewById(R.id.etSpecialization);
            etAge = findViewById(R.id.etAge);
            ivProfile = findViewById(R.id.ivProfile);
            etDoctor_gender = findViewById(R.id.etDoctor_gender);

            token = "Token " + SharedPrefManager.getInstance(this).getToken();
            apiService = ApiClient.getClient().create(ApiService.class);

            loadProfile();

            ivProfile.setOnClickListener(v -> openGallery());
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
                        etSpecialization.setText(doctor.getSpecialization());
                        etAge.setText(doctor.getAge());
                        etDoctor_gender.setText(doctor.getGender());
                        if (doctor.getProfileImage() != null) {
                            Glide.with(ProfileActivity.this)
                                    .load(ApiClient.BASE_URL + doctor.getProfileImage())
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
            Button btnUpdate = findViewById(R.id.btnUpdate);

            btnUpdate.setOnClickListener(v -> updateProfile()); // <-- Update when clicked

        }

        private void openGallery() {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivProfile.setImageURI(selectedImageUri); // Preview only
            // Do NOT update immediately here
        }
    }


    private void updateProfile() {
        RequestBody doctorId  = RequestBody.create(MediaType.parse("text/plain"), etDoctorId.getText().toString());
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), etName.getText().toString());
        RequestBody phone = RequestBody.create(MediaType.parse("text/plain"), etPhone.getText().toString());
        RequestBody age = RequestBody.create(MediaType.parse("text/plain"), etAge.getText().toString());
        RequestBody gender = RequestBody.create(MediaType.parse("text/plain"), "Male"); // you can make editable
        RequestBody specialization = RequestBody.create(MediaType.parse("text/plain"), etSpecialization.getText().toString());
        RequestBody email = RequestBody.create(
                MediaType.parse("text/plain"),
                etEmail.getText().toString()
        );

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            imagePart = MultipartBody.Part.createFormData("profile_image", file.getName(), reqFile);
        }

        apiService.updateDoctorProfile(token, doctorId,name, phone, age, gender, specialization, email,imagePart)
                .enqueue(new Callback<DoctorResponse>() {
                    @Override
                    public void onResponse(Call<DoctorResponse> call, Response<DoctorResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            loadProfile(); // reload profile after update
                        } else {
                            Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DoctorResponse> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
