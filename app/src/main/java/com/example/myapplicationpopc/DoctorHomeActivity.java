package com.example.myapplicationpopc;

import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.model.DoctorResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import retrofit2.Call;
public class DoctorHomeActivity extends AppCompatActivity {

        TextView etDoctorId, etName;
        ApiService apiService;
        String token; // saved from login

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_doctor_home);
            // Hide toolbar
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            etDoctorId = findViewById(R.id.etDoctorId);
            etName = findViewById(R.id.etName);

            token = "Token " + SharedPrefManager.getInstance(this).getToken();
            apiService = ApiClient.getClient().create(ApiService.class);
            ImageView btn1 = findViewById(R.id.imgDoctor);
            Button btnPatientManagement = findViewById(R.id.btnPatientManagement);
            Button btnSurveyRecords = findViewById(R.id.btnSurveyRecords);
            Button btnSurveyEntry = findViewById(R.id.btnSurveyEntry);
            Button btnDashboard = findViewById(R.id.btnDashboard);


            // Use lambda for click listeners
            btn1.setOnClickListener(view -> {
                Intent intent = new Intent(DoctorHomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            });

            btnPatientManagement.setOnClickListener(view -> {
                Intent intent = new Intent(DoctorHomeActivity.this, PatientManagementActivity.class);
                startActivity(intent);
            });
           btnSurveyRecords.setOnClickListener(view -> {
               Intent intent = new Intent(DoctorHomeActivity.this,SurveyListActivity.class);
               startActivity(intent);
            });

            btnDashboard.setOnClickListener(view -> {
                Intent intent = new Intent(DoctorHomeActivity.this,DetailsActivity.class);
                startActivity(intent);
            });

            loadDoctorProfile();

        }

        private void loadDoctorProfile() {
            apiService.getDoctorProfile(token).enqueue(new Callback<DoctorResponse>() {
                @Override
                public void onResponse(Call<DoctorResponse> call, Response<DoctorResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        DoctorResponse doctor = response.body();
                        etDoctorId.setText(doctor.getDoctorId());
                        etName.setText(doctor.getName());
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


