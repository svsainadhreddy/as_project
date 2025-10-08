package com.example.myapplicationpopc;

import android.os.Bundle;
import android.view.Gravity;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurveyDisplayActivity extends AppCompatActivity {

    private LinearLayout llSurveyContainer;
    private ApiService apiService;
    private int patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_display);
        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        llSurveyContainer = findViewById(R.id.llSurveyContainer);
        apiService = ApiClient.getClient().create(ApiService.class);


        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchSurvey();
    }

    private void fetchSurvey() {
        String savedToken = SharedPrefManager.getInstance(this).getToken();
        String authToken = "Token " + savedToken;

        apiService.getSurveyByPatient(authToken, patientId)
                .enqueue(new Callback<SurveyDisplayResponse>() {
                    @Override
                    public void onResponse(Call<SurveyDisplayResponse> call,
                                           Response<SurveyDisplayResponse> resp) {
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
        llSurveyContainer.removeAllViews();

        // ---- Top Card: Risk Level ----
        LinearLayout riskCard = new LinearLayout(this);
        riskCard.setOrientation(LinearLayout.VERTICAL);
        riskCard.setPadding(24, 24, 24, 24);
        riskCard.setBackgroundResource(R.drawable.rounded_card);

        TextView tvRisk = new TextView(this);
        tvRisk.setText("Risk Level: " + getRiskLevel(survey.getTotal_score()));
        tvRisk.setTextSize(18f);
        riskCard.addView(tvRisk);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        llSurveyContainer.addView(riskCard, cardParams);

        // ---- Section Cards ----
        for (SurveyDisplayResponse.SectionScore sec : survey.getSection_scores()) {

            LinearLayout sectionCard = new LinearLayout(this);
            sectionCard.setOrientation(LinearLayout.VERTICAL);
            sectionCard.setPadding(24, 24, 24, 24);
            sectionCard.setBackgroundResource(R.drawable.rounded_card);

            TextView tvSection = new TextView(this);
            tvSection.setText("Section: " + sec.getSection());
            tvSection.setTextSize(16f);
            tvSection.setPadding(0, 0, 0, 16);
            sectionCard.addView(tvSection);

            // TableLayout for Q/A
            TableLayout table = new TableLayout(this);
            table.setStretchAllColumns(true);

            for (SurveyDisplayResponse.Answer ans : survey.getAnswers()) {
                if (sec.getSection().equalsIgnoreCase(sec.getSection())) {
                    String sel = ans.getSelected_option();
                    if (sel == null || sel.isEmpty()) sel = ans.getCustom_text();

                    TableRow row = new TableRow(this);
                    TextView tvQ = new TextView(this);
                    tvQ.setText(ans.getQuestion());
                    tvQ.setPadding(0, 8, 16, 8);

                    TextView tvA = new TextView(this);
                    tvA.setText(sel);
                    tvA.setPadding(16, 8, 0, 8);
                    tvA.setGravity(Gravity.START);

                    row.addView(tvQ);
                    row.addView(tvA);
                    table.addView(row);
                }
            }

            sectionCard.addView(table);
            llSurveyContainer.addView(sectionCard, cardParams);
        }
    }

    private void showSurveyNotCompleted() {
        llSurveyContainer.removeAllViews();

        TextView tvMsg = new TextView(this);
        tvMsg.setText("Survey not completed.");
        tvMsg.setTextSize(18f);
        tvMsg.setGravity(Gravity.CENTER);
        llSurveyContainer.addView(tvMsg);
    }

    private String getRiskLevel(int score) {
        if (score <= 20) return "Low";
        else if (score <= 40) return "Moderate";
        else if (score <= 60) return "High";
        else return "Very High";
    }
}
