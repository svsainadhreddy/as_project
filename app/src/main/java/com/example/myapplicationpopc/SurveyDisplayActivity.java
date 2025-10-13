package com.example.myapplicationpopc;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.model.SurveyDisplayResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurveyDisplayActivity extends AppCompatActivity {

    private LinearLayout llSections;
    private TextView tvRiskLevel, tvRiskLevelSub, tvRiskBadge;
    private Button btnDone;
    private ApiService apiService;
    private int patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_display);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        llSections = findViewById(R.id.llSections);
        btnDone = findViewById(R.id.btnDone);
        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvRiskLevelSub = findViewById(R.id.tvRiskLevelSub);
        tvRiskBadge = findViewById(R.id.tvRiskBadge);

        apiService = ApiClient.getClient().create(ApiService.class);
        patientId = getIntent().getIntExtra("patient_id", -1);

        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnDone.setOnClickListener(v -> finish());
        fetchSurvey();
    }

    private void fetchSurvey() {
        String savedToken = SharedPrefManager.getInstance(this).getToken();
        String authToken = "Token " + savedToken;

        apiService.getSurveyByPatient(authToken, patientId)
                .enqueue(new Callback<SurveyDisplayResponse>() {
                    @Override
                    public void onResponse(Call<SurveyDisplayResponse> call, Response<SurveyDisplayResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            renderSurvey(resp.body());
                        } else {
                            showSurveyNotCompleted();
                        }
                    }

                    @Override
                    public void onFailure(Call<SurveyDisplayResponse> call, Throwable t) {
                        showSurveyNotCompleted();
                    }
                });
    }

    private void renderSurvey(SurveyDisplayResponse survey) {
        llSections.removeAllViews();

        // --- Top Risk Card ---
        String riskLevel = getRiskLevel(survey.getTotal_score());
        tvRiskLevel.setText("Risk Level: " + riskLevel);

        switch (riskLevel) {
            case "High":
                tvRiskBadge.setText("High");
                tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_orange);
                tvRiskLevelSub.setText("Coordinate care plan; follow-up in 3 months.");
                break;
            case "Low":
                tvRiskBadge.setText("Low");
                tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_green);
                tvRiskLevelSub.setText("No intervention needed.");
                break;
            default:
                tvRiskBadge.setText(riskLevel);
                tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_yellow);
                tvRiskLevelSub.setText("Monitor and provide guidance.");
                break;
        }

        List<SurveyDisplayResponse.SectionScore> sections = survey.getSection_scores();
        List<SurveyDisplayResponse.Answer> answers = survey.getAnswers();

        if (sections == null || sections.isEmpty()) {
            // Handle empty sections
            sections = new ArrayList<>();
            sections.add(new SurveyDisplayResponse.SectionScore());
        }

        for (SurveyDisplayResponse.SectionScore sec : sections) {
            String sectionName = sec.getSection() != null ? sec.getSection() : "Section 1";
            int sectionScore = sec.getScore();

            // --- Section Card ---
            LinearLayout sectionCard = new LinearLayout(this);
            sectionCard.setOrientation(LinearLayout.VERTICAL);
            sectionCard.setBackgroundResource(R.drawable.bg_risk_card);
            sectionCard.setPadding(24, 24, 24, 24);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 24);
            sectionCard.setLayoutParams(cardParams);

            // Header
            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvSection = new TextView(this);
            tvSection.setText(sectionName + " (Score: " + sectionScore + ")");
            tvSection.setTextSize(16f);
            tvSection.setTypeface(null, Typeface.BOLD);
            tvSection.setTextColor(0xFF232028);
            tvSection.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvArrow = new TextView(this);
            tvArrow.setText("\u25BC"); // ▼
            tvArrow.setTextSize(18f);
            tvArrow.setPadding(8, 0, 0, 0);
            tvArrow.setTextColor(0xFF232028);

            header.addView(tvSection);
            header.addView(tvArrow);
            sectionCard.addView(header);

            // TableLayout for Q&A
            TableLayout table = new TableLayout(this);
            table.setVisibility(View.GONE);
            table.setStretchAllColumns(true);
            table.setPadding(0, 16, 0, 0);

            List<SurveyDisplayResponse.Answer> sectionAnswers = new ArrayList<>();
            if (answers != null) {
                for (SurveyDisplayResponse.Answer ans : answers) {
                    if (ans.getQuestion() != null) {
                        sectionAnswers.add(ans); // Optionally filter by section_name if available
                    }
                }
            }

            for (SurveyDisplayResponse.Answer ans : sectionAnswers) {
                String value = ans.getSelected_option();
                if (value == null || value.isEmpty()) value = ans.getCustom_text();

                TableRow row = new TableRow(this);
                row.setPadding(0, 8, 0, 8);

                TextView tvQ = new TextView(this);
                tvQ.setText(ans.getQuestion());
                tvQ.setTextSize(14f);
                tvQ.setPadding(0, 8, 16, 8);
                tvQ.setTypeface(null, Typeface.NORMAL);

                TextView tvA = new TextView(this);
                tvA.setText(value);
                tvA.setTextSize(14f);
                tvA.setPadding(16, 8, 0, 8);
                tvA.setTypeface(null, Typeface.BOLD);

                row.addView(tvQ);
                row.addView(tvA);
                table.addView(row);
            }

            sectionCard.addView(table);

            // Expand/Collapse
            header.setOnClickListener(v -> {
                boolean expanded = table.getVisibility() == View.VISIBLE;
                table.setVisibility(expanded ? View.GONE : View.VISIBLE);
                tvArrow.setText(expanded ? "\u25BC" : "\u25B2"); // ▼ or ▲
            });

            llSections.addView(sectionCard);
        }
    }

    private void showSurveyNotCompleted() {
        llSections.removeAllViews();
        TextView tvMsg = new TextView(this);
        tvMsg.setText("Survey not completed.");
        tvMsg.setTextSize(18f);
        tvMsg.setGravity(Gravity.CENTER);
        llSections.addView(tvMsg);
    }

    private String getRiskLevel(int score) {
        if (score <= 20) return "Low";
        else if (score <= 40) return "Moderate";
        else if (score <= 60) return "High";
        else return "Very High";
    }
}
