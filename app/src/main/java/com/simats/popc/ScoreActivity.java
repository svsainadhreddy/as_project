package com.simats.popc;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.simats.popc.model.SurveySectionRisk;
import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScoreActivity extends AppCompatActivity {

    private int patientId;
    private LinearLayout llSections;
    private TextView tvTotalScore, tvRiskLevel, tvRiskScale, tvManagement;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        llSections = findViewById(R.id.llSections);
        tvTotalScore = findViewById(R.id.tvTotalScore);
        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvRiskScale = findViewById(R.id.tvRiskScale);
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

            LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.item_score_box, llSections, false);
            TextView tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            TextView tvCategoryScore = itemView.findViewById(R.id.tvCategoryScore);

            tvCategoryName.setText(s.getSection_name() + " Score");
            tvCategoryScore.setText(String.valueOf(s.getScore()));

            llSections.addView(itemView);
        }

        tvTotalScore.setText(String.valueOf(totalScore));

        String level = getRiskLevel(totalScore);
        tvRiskLevel.setText(level);

        setRiskTextBackground(level);   // ⬅ only text bg changes
        tvRiskScale.setText(getHighlightedScale(level));

        tvManagement.setText(calculateManagementAdvice(totalScore));
    }

    // --------------------------------------------
    // HIGHLIGHT ACTIVE RISK RANGE
    // --------------------------------------------
    private String getHighlightedScale(String level) {

        String low = "Low: 0–20";
        String moderate = "Moderate: 21–40";
        String high = "High: 41–60";
        String veryHigh = "Very High: >60";

        String current = "";

        if (level.startsWith("Low")) current = low;
        else if (level.startsWith("Moderate")) current = moderate;
        else if (level.startsWith("High Risk")) current = high;
        else if (level.startsWith("Very High")) current = veryHigh;

        return low.replace(current, "👉 " + current.toUpperCase() + " 👈")
                + "  |  " +
                moderate.replace(current, "👉 " + current.toUpperCase() + " 👈")
                + "  |  " +
                high.replace(current, "👉 " + current.toUpperCase() + " 👈")
                + "  |  " +
                veryHigh.replace(current, "👉 " + current.toUpperCase() + " 👈");
    }

    // --------------------------------------------
    // ONLY RISK TEXT BACKGROUND COLOR
    // --------------------------------------------
    private void setRiskTextBackground(String level) {

        int color;

        if (level.equals("Low Risk")) {
            color = android.graphics.Color.parseColor("#C8E6C9");
        }
        else if (level.equals("Moderate Risk")) {
            color = android.graphics.Color.parseColor("#FFF9C4");
        }
        else if (level.equals("High Risk")) {
            color = android.graphics.Color.parseColor("#FFE0B2");
        }
        else {
            color = android.graphics.Color.parseColor("#FFCDD2");
        }

        // Apply drawable + tint
        Drawable bg = AppCompatResources.getDrawable(this, R.drawable.gold_score_bg);
        if (bg != null) {
            bg = DrawableCompat.wrap(bg);
            DrawableCompat.setTint(bg, color);
            tvRiskLevel.setBackground(bg);

            // 6dp padding converted to px
            int paddingPx = (int) (9 * getResources().getDisplayMetrics().density);
            tvRiskLevel.setPadding(
                    paddingPx,
                    paddingPx,
                    paddingPx,
                    paddingPx
            );
            }

        tvRiskLevel.getBackground().setTint(color);    }

    private String getRiskLevel(int totalScore) {
        if (totalScore <= 20) return "Low Risk";
        else if (totalScore <= 40) return "Moderate Risk";
        else if (totalScore <= 60) return "High Risk";
        else return "Very High Risk";
    }

    private String calculateManagementAdvice(int totalScore) {
        if (totalScore <= 20) {
            return "STANDARD ANESTHESIA ,\nROUTINE MONITORING .";
        } else if (totalScore <= 40) {
            return "LUNG- PROTECTIVE VENTILATION ,\nMULTIMODAL ANALGESIA ,\nENCOURAGE EARLY MOBILIZATION.";
        } else if (totalScore <= 60) {
            return "PREFER REGIONAL IF FEASIBLE ,\nSTRICT LUNG- PROTECTIVE STRATEGY ,\nCONSIDER POSTOPERATIVE  ICU/HDU.";
        } else {
            return "STRONGLY CONSIDER AVOIDING GA/ETT IF POSSIBLE ,\nOPTIMIZE COMORBIDITIES PRE-OP ,\nMANDATORY ICU PLANNING.";
        }
    }
}
