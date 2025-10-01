package com.example.myapplicationpopc;


import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplicationpopc.model.DashboardResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {

    private TextView tvTotalPatientsValue, tvPendingValue, tvHighriskvalue;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tvTotalPatientsValue = findViewById(R.id.tvTotalPatientsValue);
        tvPendingValue = findViewById(R.id.tvPendingValue);
        tvHighriskvalue = findViewById(R.id.tvHighriskvalue);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        loadDashboard();
    }

    private void loadDashboard() {
        apiService.getDashboard(token).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse data = response.body();
                    tvTotalPatientsValue.setText(String.valueOf(data.getTotal_surveyed()));
                    tvPendingValue.setText(String.valueOf(data.getPending_surveys()));
                    tvHighriskvalue.setText(String.valueOf(data.getHigh_risk_patients()));
                } else {
                    Toast.makeText(DetailsActivity.this, "Failed to load dashboard", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                Toast.makeText(DetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

