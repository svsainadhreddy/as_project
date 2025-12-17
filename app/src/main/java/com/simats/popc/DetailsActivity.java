package com.simats.popc;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.popc.model.DashboardResponse;
import com.simats.popc.model.Dashboardgraph;
import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {

    private TextView tvTotalPatientsValue, tvPendingValue, tvHighriskvalue, tvTotalValue;
    private LinearLayout tvcardHighRisk;
    private PieChart pieChart;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_details);

        // ---------- Initialize Views ----------
        tvcardHighRisk = findViewById(R.id.cardHighRisk);
        ImageButton btnBack = findViewById(R.id.btnBack);
        LinearLayout btnPending = findViewById(R.id.cardPendingPatients);

        tvTotalValue = findViewById(R.id.tvTotalValue);
        tvTotalPatientsValue = findViewById(R.id.tvTotalPatientsValue);
        tvPendingValue = findViewById(R.id.tvPendingValue);
        tvHighriskvalue = findViewById(R.id.tvHighriskvalue);
        pieChart = findViewById(R.id.pieChart);

        // ---------- Setup API ----------
        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        // ---------- Click Listeners ----------
        btnBack.setOnClickListener(v ->
                startActivity(new Intent(DetailsActivity.this, DoctorHomeActivity.class))
        );

        btnPending.setOnClickListener(v ->
                startActivity(new Intent(DetailsActivity.this, PendingSurveysActivity.class))
        );

        tvcardHighRisk.setOnClickListener(v ->
                startActivity(new Intent(DetailsActivity.this, HighRiskListActivity.class))
        );

        // Initial load
        loadDashboard();
        loadDashboardGraph();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Automatically reload data when returning to this page
        loadDashboard();
        loadDashboardGraph();
    }

    /** Load overall dashboard stats */
    private void loadDashboard() {
        apiService.getDashboard(token).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse data = response.body();
                    tvTotalValue.setText(String.valueOf(data.getTotal_patients()));
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

    /** Load pie chart data (Dashboardgraph) */
    private void loadDashboardGraph() {
        apiService.getDashboardStats(token).enqueue(new Callback<Dashboardgraph>() {
            @Override
            public void onResponse(Call<Dashboardgraph> call, Response<Dashboardgraph> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Dashboardgraph graph = response.body();
                    setupPieChart(
                            (float) graph.getStable(),
                            (float) graph.getPending(),
                            (float) graph.getHigh_risk()
                    );
                } else {
                    Toast.makeText(DetailsActivity.this, "Failed to load graph data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Dashboardgraph> call, Throwable t) {
                Toast.makeText(DetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Set up pie chart visuals */
    private void setupPieChart(float stable, float pending, float highRisk) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(stable, "Low Risk"));
        entries.add(new PieEntry(pending, "Moderate Risk"));
        entries.add(new PieEntry(highRisk, "High Risk"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#00BCD4"), // Low Risk - Teal
                Color.parseColor("#FFEB3B"), // Moderate Risk - Yellow
                Color.parseColor("#F44336")  // High Risk - Red
        );
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);



        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(60f);
        pieChart.animateY(1000);
        pieChart.setHoleColor(Color.parseColor("#898A8A8A")); // Light background toneCEA7AD
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.setCenterTextSize(16f);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextColor(Color.BLACK);

        pieChart.invalidate(); // Refresh chart
    }
}
