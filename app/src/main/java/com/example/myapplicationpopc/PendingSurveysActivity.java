package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_surveys);
        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton btnBack = findViewById(R.id.btnBack);

        recyclerView = findViewById(R.id.recyclerPatients);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PendingPatientAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = SharedPrefManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this,
                    "Authentication token missing. Please login again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(PendingSurveysActivity.this,DetailsActivity.class);
            startActivity(intent);
        });
        loadPendingPatients();
    }

    private void loadPendingPatients() {
        apiService.getPendingSurveys("Token " + token)
                .enqueue(new Callback<List<PendingPatient>>() {
                    @Override
                    public void onResponse(Call<List<PendingPatient>> call,
                                           Response<List<PendingPatient>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateData(response.body());
                        } else {
                            Toast.makeText(PendingSurveysActivity.this,
                                    "Failed to load (" + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PendingPatient>> call, Throwable t) {
                        Toast.makeText(PendingSurveysActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
