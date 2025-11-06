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

import com.example.myapplicationpopc.adapter.PendingPatientAdapter;
import com.example.myapplicationpopc.model.PendingPatient;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingSurveysActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PendingPatientAdapter adapter;
    private ApiService apiService;
    private String token;
    private List<PendingPatient> patientList = new ArrayList<>();
    private List<PendingPatient> filteredList = new ArrayList<>();
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_surveys);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        ImageView btnProfile= findViewById(R.id.btnProfile);
        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerPatients);
        etSearch = findViewById(R.id.etSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingPatientAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = SharedPrefManager.getInstance(this).getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Authentication token missing. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        btnProfile.setOnClickListener(view -> {
            Intent intent = new Intent(PendingSurveysActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(PendingSurveysActivity.this, DetailsActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadPendingPatients();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Automatically reload data when returning to this page
        loadPendingPatients();
    }

    private void loadPendingPatients() {
        apiService.getPendingSurveys("Token " + token)
                .enqueue(new Callback<List<PendingPatient>>() {
                    @Override
                    public void onResponse(Call<List<PendingPatient>> call, Response<List<PendingPatient>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            patientList.clear();
                            patientList.addAll(response.body());
                            filteredList.clear();
                            filteredList.addAll(patientList);
                            adapter.updateData(filteredList);
                        } else {
                            Toast.makeText(PendingSurveysActivity.this,
                                    "Failed to load (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PendingPatient>> call, Throwable t) {
                        Toast.makeText(PendingSurveysActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterPatients(String query) {
        if (query == null) query = "";
        query = query.trim().toLowerCase();
        filteredList.clear();

        for (PendingPatient p : patientList) {
            String name = (p.getName() != null) ? p.getName().toLowerCase() : "";
            String id = (p.getId() != null) ? p.getId().toLowerCase() : "";

            if (name.contains(query) || id.contains(query)) {
                filteredList.add(p);
            }
        }

        adapter.updateData(filteredList);
    }


}
