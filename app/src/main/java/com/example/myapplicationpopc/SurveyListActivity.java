package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationpopc.adapter.RecordAdapter;
import com.example.myapplicationpopc.model.RecordsResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurveyListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private RecordAdapter adapter;
    private List<RecordsResponse> patientList = new ArrayList<>();
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_list);
        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton btnBack = findViewById(R.id.btnBack);

        recycler = findViewById(R.id.recyclerPatients);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        //back button

        // ✅ Adapter needs PatientResponse
        adapter = new RecordAdapter(this, patientList, patient -> {
            // 👉 When arrow is clicked, go to SurveyDisplayActivity
            Intent i = new Intent(SurveyListActivity.this, SurveyDisplayActivity.class);
            i.putExtra("patient_id", patient.getPk());   // send patient id
            startActivity(i);
        });


        recycler.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(SurveyListActivity.this,DoctorHomeActivity.class);
            startActivity(intent);
        });
        loadPatients();

    }

    private void loadPatients() {
        apiService.listCompletedPatients(token).enqueue(new Callback<List<RecordsResponse>>() {
            @Override
            public void onResponse(Call<List<RecordsResponse>> call,
                                   Response<List<RecordsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    patientList.clear();
                    patientList.addAll(response.body());
                    adapter.setList(patientList);
                } else {
                    Toast.makeText(SurveyListActivity.this,
                            "No completed surveys found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RecordsResponse>> call, Throwable t) {
                Toast.makeText(SurveyListActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
