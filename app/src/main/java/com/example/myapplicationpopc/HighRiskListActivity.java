package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationpopc.adapter.HighRiskRecordAdapter;
import com.example.myapplicationpopc.model.RecordsResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HighRiskListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private HighRiskRecordAdapter adapter;
    private List<RecordsResponse> patientList = new ArrayList<>();
    private List<RecordsResponse> filteredList = new ArrayList<>();
    private ApiService apiService;
    private String token;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_list); // ✅ same parent layout, only item differs

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        ImageView btnProfile = findViewById(R.id.btnProfile);
        ImageView btnBack = findViewById(R.id.btnBack);
        recycler = findViewById(R.id.recyclerPatients);
        etSearch = findViewById(R.id.etSearch);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Use HighRiskRecordAdapter (different item layout: item_surveypatient.xml)
        adapter = new HighRiskRecordAdapter(this, filteredList, patient -> {
            Intent i = new Intent(HighRiskListActivity.this, SurveyDisplayActivity.class);
            i.putExtra("patient_id", patient.getPk());
            startActivity(i);
        });
        recycler.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        btnProfile.setOnClickListener(view -> {
            Intent intent = new Intent(HighRiskListActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(HighRiskListActivity.this, DoctorHomeActivity.class);
            startActivity(intent);
        });

        // 🧠 Search logic
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadHighRiskPatients();
    }

    // 🩸 API call to get high-risk patients
    private void loadHighRiskPatients() {
        apiService.listHighRiskPatients(token).enqueue(new Callback<List<RecordsResponse>>() {
            @Override
            public void onResponse(Call<List<RecordsResponse>> call, Response<List<RecordsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    patientList.clear();
                    patientList.addAll(response.body());
                    filteredList.clear();
                    filteredList.addAll(patientList);
                    adapter.setList(filteredList);
                } else {
                    Toast.makeText(HighRiskListActivity.this, "No high-risk patients found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RecordsResponse>> call, Throwable t) {
                Toast.makeText(HighRiskListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔍 Filter function
    private void filterPatients(String query) {
        if (query == null) query = "";
        query = query.trim().toLowerCase();
        filteredList.clear();

        for (RecordsResponse p : patientList) {
            String name = (p.getName() != null) ? p.getName().toLowerCase() : "";
            String id = (p.getId() != null) ? p.getId().toLowerCase() : "";

            if (name.contains(query) || id.contains(query)) {
                filteredList.add(p);
            }
        }

        adapter.setList(filteredList);
    }
}
