package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationpopc.adapter.PatientAdapter;
import com.example.myapplicationpopc.model.PatientResponse;
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
    private PatientAdapter adapter;
    private List<PatientResponse> patientList = new ArrayList<>();
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_list);

        recycler = findViewById(R.id.recyclerPatients);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Adapter needs PatientResponse
        adapter = new PatientAdapter(this, patientList, patient -> {
            // 👉 When arrow is clicked, go to SurveyDisplayActivity
            Intent i = new Intent(SurveyListActivity.this, SurveyDisplayActivity.class);
            i.putExtra("patient_id", patient.getId());   // send patient id
            startActivity(i);
        });
        recycler.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        loadPatients();
    }

    private void loadPatients() {
        // ✅ Make sure your ApiService returns List<PatientResponse>
        apiService.listPatients(token, null).enqueue(new Callback<List<PatientResponse>>() {
            @Override
            public void onResponse(Call<List<PatientResponse>> call,
                                   Response<List<PatientResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    patientList.clear();
                    patientList.addAll(response.body());
                    adapter.setList(patientList);
                } else {
                    Toast.makeText(SurveyListActivity.this,
                            "Failed to load patients", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PatientResponse>> call, Throwable t) {
                Toast.makeText(SurveyListActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
