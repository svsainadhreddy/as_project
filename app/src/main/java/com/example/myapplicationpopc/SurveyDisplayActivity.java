package com.example.myapplicationpopc;

import android.os.Bundle;
import android.widget.LinearLayout;
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

    private LinearLayout llContainer;
    private ApiService apiService;
    private int patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_display);

        llContainer = findViewById(R.id.llContainer);
        apiService = ApiClient.getClient().create(ApiService.class);

        // ✅ Get patient_id from adapter
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
                            Toast.makeText(SurveyDisplayActivity.this,
                                    "Fetch failed: " + resp.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SurveyDisplayResponse> call, Throwable t) {
                        Toast.makeText(SurveyDisplayActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void renderSurvey(SurveyDisplayResponse survey) {
        llContainer.removeAllViews();

        // Patient + survey info
        TextView tvMeta = new TextView(this);
        tvMeta.setText("Patient ID: " + patientId
                + " \n Status: " + survey.getStatus()
                + "  Total Score: " + survey.getTotal_score());
        llContainer.addView(tvMeta);

        // Section scores
        for (SurveyDisplayResponse.SectionScore sec : survey.getSection_scores()) {
            TextView tvSec = new TextView(this);
            tvSec.setText("\nSection: " + sec.getSection()
                    + " — Score: " + sec.getScore());
            llContainer.addView(tvSec);
        }

        // Questions + options
        for (SurveyDisplayResponse.Answer ans : survey.getAnswers()) {
            TextView tvQ = new TextView(this);
            tvQ.setText("Q: " + ans.getQuestion());
            llContainer.addView(tvQ);

            TextView tvSel = new TextView(this);
            String sel = ans.getSelected_option();
            if (sel == null || sel.isEmpty()) {
                sel = ans.getCustom_text();
            }
            tvSel.setText("Answer: " + sel + " (score: " + ans.getScore() + ")");
            llContainer.addView(tvSel);
        }
    }
}
