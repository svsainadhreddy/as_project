package com.example.myapplicationpopc;

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

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurveyDisplayActivity extends AppCompatActivity {

    private LinearLayout llSections;
    private TextView tvRiskLevel, tvRiskLevelSub, tvRiskBadge;
    private Button btnDone;
    private ApiService apiService;
    private int patientId;

    private Map<String, Boolean> expandedSections = new HashMap<>();

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

        // --- Top card with total risk level ---
        String level = getRiskLevel(survey.getTotal_score());
        tvRiskLevel.setText("Risk Level: " + level);

        if (level.equals("High")) {
            tvRiskBadge.setText("High");
            tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_orange);
            tvRiskLevelSub.setText("Prefer regional if feasible, strict lung-protective strategy, consider postoperative ICU/HDU.");
        } else if (level.equals("Very high")) {
            tvRiskBadge.setText("Very high");
            tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_green);
            tvRiskLevelSub.setText("Strongly consider avoiding GA/ETT if possible; optimize comorbidities pre-op, mandatory ICU planning.");
        } else if (level.equals("Moderate")) {
            tvRiskBadge.setText("Moderate");
            tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_green);
            tvRiskLevelSub.setText("Lung-protective ventilation, multimodal analgesia, encourage early mobilization.");
        } else {
            tvRiskBadge.setText(level);
            tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_yellow);
            tvRiskLevelSub.setText("Standard anesthesia; routine monitoring.");
        }

        // --- Render each section individually ---
        for (SurveyDisplayResponse.SectionScore sec : survey.getSection_scores()) {
            String sectionName = sec.getSection();
            int sectionScore = sec.getScore();

            // Section card container
            LinearLayout sectionCard = new LinearLayout(this);
            sectionCard.setOrientation(LinearLayout.VERTICAL);
            sectionCard.setBackgroundResource(R.drawable.bg_risk_card);
            sectionCard.setPadding(16, 16, 16, 30);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 40);
            sectionCard.setLayoutParams(cardParams);

            // Header layout
            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvHeader = new TextView(this);
            tvHeader.setText(sectionName);
            tvHeader.setTextSize(17f);
            tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            tvHeader.setTextColor(0xFF232028);
            tvHeader.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvScore = new TextView(this);
            tvScore.setText(String.valueOf(sectionScore));
            tvScore.setTextSize(13f);
            tvScore.setTextColor(0xFF232028);
            tvScore.setPadding(16, 6, 16, 6);
            tvScore.setBackgroundResource(R.drawable.bg_badge_gray);
            tvScore.setGravity(Gravity.CENTER);

            TextView tvArrow = new TextView(this);
            tvArrow.setText("\u25BC"); // ▼
            tvArrow.setTextSize(18f);
            tvArrow.setPadding(10, 0, 0, 0);
            tvArrow.setTextColor(0xFF232028);

            header.addView(tvHeader);
            header.addView(tvScore);
            header.addView(tvArrow);

            sectionCard.addView(header);

            // Table layout for Q/A
            TableLayout table = new TableLayout(this);
            table.setVisibility(View.GONE);
            table.setStretchAllColumns(true);
            table.setPadding(0, 16, 0, 8);

            for (SurveyDisplayResponse.Answer ans : survey.getAnswers()) {
                if (ans != null && ans.getQuestion() != null &&
                        ans.getSelected_option() != null &&
                        sectionName.equalsIgnoreCase(ans.getQuestion())) {
                    // do nothing, this is wrong — fix:
                }

                // Only include answers for this section
                if (ans != null && ans.getQuestion() != null && ans.getSelected_option() != null &&
                        ans.getScore() >= 0) {
                    // Compare section
                    String answerSection = ans.getSectionName();  // or ans.getSection_name() if present
                    // fallback to null-safe check
                    if (answerSection != null && answerSection.equalsIgnoreCase(sectionName)) {
                        String value = ans.getSelected_option();
                        if (value == null || value.isEmpty()) value = ans.getCustom_text();

                        TableRow row = new TableRow(this);

                        TextView tvQ = new TextView(this);
                        tvQ.setText(ans.getQuestion());
                        tvQ.setPadding(0, 8, 8, 8);
                        tvQ.setTextSize(14f);

                        TextView tvA = new TextView(this);
                        tvA.setText(value);
                        tvA.setPadding(8, 8, 0, 8);
                        tvA.setTextSize(14f);
                        tvA.setTypeface(null, android.graphics.Typeface.BOLD);

                        row.addView(tvQ);
                        row.addView(tvA);
                        table.addView(row);
                    }
                }
            }

            sectionCard.addView(table);

            // Expand/collapse toggle
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
