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

public class EditPatientListActivity extends AppCompatActivity {

    RecyclerView recyclerPatients;
    EditText etSearch;
    ImageView btnBack, btnProfile;

    PatientAdapter adapter;
    List<PatientResponse> patientList = new ArrayList<>();
    List<PatientResponse> filteredList = new ArrayList<>();
    ApiService apiService;
    String token;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient_list);
        // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerPatients = findViewById(R.id.recyclerPatients);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnProfile = findViewById(R.id.btnProfile);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        adapter = new PatientAdapter(this, filteredList, patient -> {
            Intent i = new Intent(EditPatientListActivity.this, EditPatientActivity.class);
            i.putExtra("patient_id", patient.getId());
            startActivity(i);
        });

        recyclerPatients.setLayoutManager(new LinearLayoutManager(this));
        recyclerPatients.setAdapter(adapter);

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


        // ✅ open PatientManagementActivity
        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(this, PatientManagementActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });

        // ✅ back to ProfileActivity
        btnProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });
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
                    Toast.makeText(EditPatientListActivity.this, "Failed to load patients", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PatientResponse>> call, Throwable t) {
                Toast.makeText(EditPatientListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
}
