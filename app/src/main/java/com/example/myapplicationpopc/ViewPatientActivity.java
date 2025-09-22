package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplicationpopc.model.PatientResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewPatientActivity extends AppCompatActivity {

    TextView tvPatientId, tvName, tvAge, tvPhone, tvWeight, tvGender, tvHeight, tvBMI;
    ImageView ivPhoto,btn1;
    ApiService apiService;
    String token;
    int patientId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient);
        // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvPatientId = findViewById(R.id.etPatientId);
        tvName = findViewById(R.id.etName);
        tvAge = findViewById(R.id.etAge);
        tvPhone = findViewById(R.id.etPhone);
        tvWeight = findViewById(R.id.etWeight);
        tvGender = findViewById(R.id.etGender);
        tvHeight = findViewById(R.id.etHeight);
        tvBMI = findViewById(R.id.etBMI);
        ivPhoto = findViewById(R.id.imgPatient);
        btn1 = findViewById(R.id.btnBack);


        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId != -1) loadPatient(patientId);
        else Toast.makeText(this, "No patient id", Toast.LENGTH_SHORT).show();
        // back to ViewPatientListActivity
        btn1.setOnClickListener(v -> {
            Intent i = new Intent(this, ViewPatientListActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });

    }

    private void loadPatient(int id) {
        apiService.getPatient(token, id).enqueue(new Callback<PatientResponse>() {
            @Override
            public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientResponse p = response.body();
                    tvPatientId.setText(p.getPatientId());
                    tvName.setText(p.getName());
                    tvAge.setText(p.getAge());
                    tvPhone.setText(p.getPhone());
                    tvWeight.setText(p.getWeight());
                    tvGender.setText(p.getGender());
                    tvHeight.setText(p.getHeight());
                    tvBMI.setText(p.getBmi());

                    if (p.getPhotoUrl() != null && !p.getPhotoUrl().isEmpty()) {
                        Glide.with(ViewPatientActivity.this).load(ApiClient.BASE_URL + p.getPhotoUrl()).into(ivPhoto);
                    }
                } else {
                    Toast.makeText(ViewPatientActivity.this, "Failed to fetch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PatientResponse> call, Throwable t) {
                Toast.makeText(ViewPatientActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
