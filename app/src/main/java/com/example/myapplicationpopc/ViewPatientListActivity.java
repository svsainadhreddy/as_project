package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
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

public class ViewPatientListActivity extends AppCompatActivity {

    RecyclerView recycler;
    EditText etSearch;
    ApiService apiService;
    String token;
    PatientAdapter adapter;
    List<PatientResponse> patients = new ArrayList<>();
    String mode = "view";
    ImageView btn1, btn2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient_list);
        // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recycler = findViewById(R.id.recyclerPatients);
        etSearch = findViewById(R.id.etSearch);
        btn1 = findViewById(R.id.btnBack);
        btn2 = findViewById(R.id.btnProfile);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        if (getIntent().hasExtra("mode")) {
            mode = getIntent().getStringExtra("mode");
        }

        adapter = new PatientAdapter(this, patients, patient -> {
            Intent i;
            if ("edit".equals(mode)) {
                i = new Intent(ViewPatientListActivity.this, EditPatientActivity.class);
            } else {
                i = new Intent(ViewPatientListActivity.this, ViewPatientActivity.class);
            }
            i.putExtra("patient_id", patient.getId());
            startActivity(i);
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

        // ✅ open PatientManagementActivity
        btn1.setOnClickListener(v -> {
            Intent i = new Intent(this, PatientManagementActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });

        // ✅ back to ProfileActivity
        btn2.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
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
