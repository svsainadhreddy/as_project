package com.simats.popc;

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

import com.simats.popc.adapter.DeletePatientAdapter;
import com.simats.popc.model.PatientResponse;
import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeletePatientActivity extends AppCompatActivity {

    RecyclerView recyclerPatients;
    EditText etSearch;
    ImageView btnBack,btnProfile;

    DeletePatientAdapter adapter;
    List<PatientResponse> patientList = new ArrayList<>();
    List<PatientResponse> filteredList = new ArrayList<>();
    ApiService apiService;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_patient);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerPatients = findViewById(R.id.recyclerPatients);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnProfile = findViewById(R.id.btnProfile);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        adapter = new DeletePatientAdapter(this, filteredList, this::deletePatient);

        recyclerPatients.setLayoutManager(new LinearLayoutManager(this));
        recyclerPatients.setAdapter(adapter);

        // ✅ back to ProfileActivity
        btnProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });
        btnBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        loadPatients();
    }

    private void loadPatients() {
        apiService.listPatients(token, null).enqueue(new Callback<List<PatientResponse>>() {
            @Override
            public void onResponse(Call<List<PatientResponse>> call, Response<List<PatientResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    patientList = response.body();
                    filteredList.clear();
                    filteredList.addAll(patientList);
                    adapter.setList(filteredList);
                } else {
                    Toast.makeText(DeletePatientActivity.this, "Failed to load patients", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PatientResponse>> call, Throwable t) {
                Toast.makeText(DeletePatientActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterPatients(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(patientList);
        } else {
            for (PatientResponse patient : patientList) {
                if (patient.getName().toLowerCase().contains(query.toLowerCase()) ||
                        patient.getPatientId().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(patient);
                }
            }
        }
        adapter.setList(filteredList);
    }

    private void deletePatient(PatientResponse patient) {
        apiService.deletePatient(token, String.valueOf(patient.getId()))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(DeletePatientActivity.this, "Patient deleted", Toast.LENGTH_SHORT).show();
                            filteredList.remove(patient);
                            patientList.remove(patient);
                            adapter.setList(filteredList);
                        } else {
                            Toast.makeText(DeletePatientActivity.this, "Failed to delete patient", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(DeletePatientActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
