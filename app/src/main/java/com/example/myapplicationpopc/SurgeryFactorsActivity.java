package com.example.myapplicationpopc;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.model.SurveyRequest;
import com.example.myapplicationpopc.model.SurveyRequest.Answer;
import com.example.myapplicationpopc.model.SurveyRequest.SectionScore;
import com.example.myapplicationpopc.model.SurveyResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurgeryFactorsActivity extends AppCompatActivity {

    private RadioGroup rgSurgeryType, rgUrgency, rgDuration, rgBloodLoss;
    private EditText etOtherSurgery;
    private Button btnNext;
    private ImageButton btnBack;

    // 👉 Scores from previous screens
    private int patientScore = 0;
    private int medicalScore = 0;
    private int preopScore   = 0;
    private int patientId    = -1;

    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgery_factors);

        // ✅ Receive previous scores
        Intent fromPrev = getIntent();
        patientScore = fromPrev.getIntExtra("patient_score", 0);
        medicalScore = fromPrev.getIntExtra("medical_score", 0);
        preopScore   = fromPrev.getIntExtra("preop_score", 0);
        patientId    = fromPrev.getIntExtra("patient_id", -1);

        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        // --- Bind Views ---
        rgSurgeryType = findViewById(R.id.rgSurgeryType);
        rgUrgency     = findViewById(R.id.rgUrgency);
        rgDuration    = findViewById(R.id.rgDuration);
        rgBloodLoss   = findViewById(R.id.rgBloodLoss);
        etOtherSurgery= findViewById(R.id.etOtherSurgery);
        btnNext       = findViewById(R.id.btnNext);
        btnBack       = findViewById(R.id.btnBack);

        // Show textbox if "Others" is chosen
        rgSurgeryType.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb != null && rb.getText().toString().equalsIgnoreCase("Others")) {
                etOtherSurgery.setVisibility(View.VISIBLE);
            } else {
                etOtherSurgery.setVisibility(View.GONE);
            }
        });

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScoreAndSend());
    }

    private void calculateScoreAndSend() {
        int tmpScore = 0;
        List<Answer> answers = new ArrayList<>();

        // --- Type of surgery ---
        int idType = rgSurgeryType.getCheckedRadioButtonId();
        if (idType != -1) {
            RadioButton rb = findViewById(idType);
            String type = rb.getText().toString();
            if (type.equalsIgnoreCase("Others")) {
                String otherTxt = etOtherSurgery.getText().toString().trim();
                if (!otherTxt.isEmpty()) type = type + " (" + otherTxt + ")";
            }
            int s = 0;
            switch (rb.getText().toString()) {
                case "Thoracic":           s = 7; break;
                case "Upper abdominal":    s = 5; break;
                case "Lower abdominal":    s = 3; break;
                case "Neurosurgery":       s = 3; break;
                case "Orthopedic":         s = 2; break;
                case "Ent / Head & neck":  s = 2; break;
                case "Vascular / Cardiac": s = 7; break;
                case "Others":             s = 1; break;
            }
            tmpScore += s;
            answers.add(new Answer("Type of surgery", type, s));
        }

        // --- Urgency ---
        int idUrg = rgUrgency.getCheckedRadioButtonId();
        if (idUrg != -1) {
            RadioButton rb = findViewById(idUrg);
            String urgency = rb.getText().toString();
            int s = urgency.equalsIgnoreCase("Emergency") ? 4 : 0;
            tmpScore += s;
            answers.add(new Answer("Urgency", urgency, s));
        }

        // --- Duration ---
        int idDur = rgDuration.getCheckedRadioButtonId();
        if (idDur != -1) {
            RadioButton rb = findViewById(idDur);
            String duration = rb.getText().toString();
            int s = (duration.contains("2–4") || duration.contains("2-4")) ? 3 :
                    (duration.contains(">4") || duration.contains("gt")) ? 5 : 0;
            tmpScore += s;
            answers.add(new Answer("Duration", duration, s));
        }

        // --- Estimated Blood Loss ---
        int idLoss = rgBloodLoss.getCheckedRadioButtonId();
        if (idLoss != -1) {
            RadioButton rb = findViewById(idLoss);
            String loss = rb.getText().toString();
            int s = (loss.contains("500–1000") || loss.contains("500-1000")) ? 2 :
                    (loss.contains(">1000") || loss.contains("gt")) ? 3 : 0;
            tmpScore += s;
            answers.add(new Answer("Estimated blood loss", loss, s));
        }

        if (answers.isEmpty()) {
            Toast.makeText(this,
                    "Please answer at least one question",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final int surgeryScore = tmpScore;
        final int combinedTotal = patientScore + medicalScore + preopScore + surgeryScore;

        // --- Build SurveyRequest ---
        SurveyRequest request = new SurveyRequest();
        request.setPatient_id(patientId);
        request.setTotal_score(surgeryScore);

        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Surgery Factors", surgeryScore));
        request.setSection_scores(sections);
        request.setAnswers(answers);

        // --- POST to Django ---
        apiService.createSurvey(token, request).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SurgeryFactorsActivity.this,
                            "Surgery data saved. Score: " + surgeryScore,
                            Toast.LENGTH_SHORT).show();

                    // ✅ Pass all accumulated scores to PlannedAnesthesiaActivity
                    Intent intent = new Intent(SurgeryFactorsActivity.this,
                            PlannedAnesthesiaActivity.class);
                    intent.putExtra("patient_id", patientId);
                    intent.putExtra("patient_score", patientScore);
                    intent.putExtra("medical_score", medicalScore);
                    intent.putExtra("preop_score", preopScore);
                    intent.putExtra("surgery_score", surgeryScore);
                    intent.putExtra("combined_total", combinedTotal);
                    intent.putExtra("survey_id", response.body().getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SurgeryFactorsActivity.this,
                            "Save failed: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(SurgeryFactorsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
