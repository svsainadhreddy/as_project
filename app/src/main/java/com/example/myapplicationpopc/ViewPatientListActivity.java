package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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

public class ViewPatientListActivity extends AppCompatActivity {

    RecyclerView recycler;
    EditText etSearch;
    ApiService apiService;
    String token;
    PatientAdapter adapter;
    List<PatientResponse> patients = new ArrayList<>();
    String mode = "view";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient_list);

        recycler = findViewById(R.id.recyclerPatients);
        etSearch = findViewById(R.id.etSearch);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        if (getIntent().hasExtra("mode")) {
            mode = getIntent().getStringExtra("mode");
        }

        adapter = new PatientAdapter(this, patients, patient -> {
            if ("edit".equals(mode)) {
                Intent i = new Intent(ViewPatientListActivity.this, EditPatientActivity.class);
                i.putExtra("patient_id", patient.getId());
                startActivity(i);
            } else {
                Intent i = new Intent(ViewPatientListActivity.this, ViewPatientActivity.class);
                i.putExtra("patient_id", patient.getId());
                startActivity(i);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        loadPatients(null);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){}
            @Override public void afterTextChanged(Editable s){
                loadPatients(s.toString().trim());
            }
        });
    }

    private void loadPatients(String query) {
        apiService.listPatients(token, query).enqueue(new Callback<List<PatientResponse>>() {
            @Override
            public void onResponse(Call<List<PatientResponse>> call, Response<List<PatientResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    patients = response.body();
                    adapter.setList(patients);
                } else {
                    Toast.makeText(ViewPatientListActivity.this, "Failed to load", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<List<PatientResponse>> call, Throwable t) {
                Toast.makeText(ViewPatientListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
