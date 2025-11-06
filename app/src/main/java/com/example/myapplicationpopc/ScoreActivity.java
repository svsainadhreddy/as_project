package com.example.myapplicationpopc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.model.SurveySectionRisk;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScoreActivity extends AppCompatActivity {

    private int patientId;
    private LinearLayout llSections;
    private TextView tvTotalScore, tvManagement;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        llSections = findViewById(R.id.llSections);
        tvTotalScore = findViewById(R.id.tvTotalScore);
        tvManagement = findViewById(R.id.tvManagement);
        btnDone = findViewById(R.id.btnDone);

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "⚠️ Invalid patient ID", Toast.LENGTH_LONG).show();
            return;
        }

        fetchSurveySections();

        btnDone.setOnClickListener(v -> finish());
    }

    private void fetchSurveySections() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getPatientSurveyRisk("Token " + token, patientId)
                .enqueue(new Callback<List<SurveySectionRisk>>() {
                    @Override
                    public void onResponse(Call<List<SurveySectionRisk>> call, Response<List<SurveySectionRisk>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            displaySections(response.body());
                        } else {
                            Toast.makeText(ScoreActivity.this, "Failed to fetch sections", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<SurveySectionRisk>> call, Throwable t) {
                        Toast.makeText(ScoreActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displaySections(List<SurveySectionRisk> sections) {
        llSections.removeAllViews();
        int totalScore = 0;

        LayoutInflater inflater = LayoutInflater.from(this);

        for (SurveySectionRisk s : sections) {
            totalScore += s.getScore();

            // Inflate item_score_box layout
            LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.item_score_box, llSections, false);
            TextView tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            TextView tvCategoryScore = itemView.findViewById(R.id.tvCategoryScore);

            tvCategoryName.setText(s.getSection_name() + " Score");
            tvCategoryScore.setText(String.valueOf(s.getScore()));

            llSections.addView(itemView);
        }

        // Set total score
        tvTotalScore.setText(String.valueOf(totalScore));

        // Set management advice based on risk level (highest risk among sections)
        String advice = calculateManagementAdvice(sections);
        tvManagement.setText(advice);
    }

    private String calculateManagementAdvice(List<SurveySectionRisk> sections) {
        int totalScore = 0;
        for (SurveySectionRisk s : sections) {
            totalScore += s.getScore();
        }

        if (totalScore <= 20) {
            return "Standard anesthesia; routine monitoring.";
        } else if (totalScore <= 40) {
            return  "Lung-protective ventilation, multimodal analgesia, encourage early mobilization.";
        } else if (totalScore <= 60) {
            return "Prefer regional if feasible, strict lung-protective strategy, consider postoperative ICU/HDU.";
        } else {
            return "Strongly consider avoiding GA/ETT if possible; optimize comorbidities pre-op, mandatory ICU planning.";
        }
    }

}
