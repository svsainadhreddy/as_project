package com.example.myapplicationpopc;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.myapplicationpopc.model.DoctorResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorHomeActivity extends AppCompatActivity {

    TextView etDoctorId, etName;
    ImageView imgDoctor;
    ApiService apiService;
    String token; // saved from login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etDoctorId = findViewById(R.id.tvDoctorId);
        etName = findViewById(R.id.tvDoctorName);
        imgDoctor = findViewById(R.id.imgDoctor);

        token = "Token " + SharedPrefManager.getInstance(this).getToken();
        apiService = ApiClient.getClient().create(ApiService.class);

        LinearLayout btnPatientManagement = findViewById(R.id.btnPatientManagement);
        LinearLayout btnSurveyRecords = findViewById(R.id.btnReports);
        LinearLayout btnDashboard = findViewById(R.id.btnDashboard);
        LinearLayout btnSettings = findViewById(R.id.btnSettings);

        imgDoctor.setOnClickListener(view -> {
            startActivity(new Intent(DoctorHomeActivity.this, ProfileActivity.class));
        });

        btnPatientManagement.setOnClickListener(view -> {
            startActivity(new Intent(DoctorHomeActivity.this, PatientManagementActivity.class));
        });

        btnSurveyRecords.setOnClickListener(view -> {
            startActivity(new Intent(DoctorHomeActivity.this, SurveyListActivity.class));
        });

        btnDashboard.setOnClickListener(view -> {
            startActivity(new Intent(DoctorHomeActivity.this, DetailsActivity.class));
        });

        btnSettings.setOnClickListener(view -> {
            startActivity(new Intent(DoctorHomeActivity.this, SettingsActivity.class));
        });

        loadDoctorProfile();
    }

    private void loadDoctorProfile() {
        apiService.getDoctorimg(token).enqueue(new Callback<DoctorResponse>() {
            @Override
            public void onResponse(Call<DoctorResponse> call, Response<DoctorResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DoctorResponse doctor = response.body();
                    etDoctorId.setText(doctor.getDoctorId());
                    etName.setText(doctor.getName());

                    String imageUrl = doctor.getProfileImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(DoctorHomeActivity.this)
                                .load(imageUrl)  // Correct: Using absolute URL directly from API
                                .placeholder(R.drawable.ic_person_outline)
                                .error(R.drawable.ic_person_outline)
                                .into(imgDoctor);
                    } else {
                        imgDoctor.setImageResource(R.drawable.ic_person_outline);
                    }

                } else {
                    Toast.makeText(DoctorHomeActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<DoctorResponse> call, Throwable t) {
                Toast.makeText(DoctorHomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
