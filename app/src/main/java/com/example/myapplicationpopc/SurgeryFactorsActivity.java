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

    private int patientId;
    private ApiService apiService;
    private String token;

    private static int checkedType = -1, checkedUrg = -1, checkedDur = -1, checkedLoss = -1;
    private static String otherText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgery_factors);

        initViews();
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        String savedToken = SharedPrefManager.getInstance(this).getToken();
        if (savedToken != null && !savedToken.trim().isEmpty()) token = "Token " + savedToken.trim();
        else {
            Toast.makeText(this, "Authentication token missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Restore selections
        rgSurgeryType.check(checkedType);
        rgUrgency.check(checkedUrg);
        rgDuration.check(checkedDur);
        rgBloodLoss.check(checkedLoss);
        etOtherSurgery.setText(otherText);
        toggleOtherVisibility(checkedType);

        rgSurgeryType.setOnCheckedChangeListener((group, id) -> {
            checkedType = id;
            toggleOtherVisibility(id);
        });
        rgUrgency.setOnCheckedChangeListener((g, i) -> checkedUrg = i);
        rgDuration.setOnCheckedChangeListener((g, i) -> checkedDur = i);
        rgBloodLoss.setOnCheckedChangeListener((g, i) -> checkedLoss = i);

        btnBack.setOnClickListener(v -> {
            otherText = etOtherSurgery.getText().toString();
            startActivity(new Intent(this, PreoperativeConsiderationsActivity.class)
                    .putExtra("patient_id", patientId));
            finish();
        });

        btnNext.setOnClickListener(v -> {
            otherText = etOtherSurgery.getText().toString();
            sendSurvey();
        });
    }

    private void initViews() {
        rgSurgeryType = findViewById(R.id.rgSurgeryType);
        rgUrgency = findViewById(R.id.rgUrgency);
        rgDuration = findViewById(R.id.rgDuration);
        rgBloodLoss = findViewById(R.id.rgBloodLoss);
        etOtherSurgery = findViewById(R.id.etOtherSurgery);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
    }

    private void toggleOtherVisibility(int id) {
        if (id != -1) {
            RadioButton rb = findViewById(id);
            if (rb != null && "Others".equalsIgnoreCase(rb.getText().toString())) {
                etOtherSurgery.setVisibility(View.VISIBLE);
                return;
            }
        }
        etOtherSurgery.setVisibility(View.GONE);
    }

    private void sendSurvey() {
        List<Answer> answers = new ArrayList<>();
        int totalScore = 0;

        // Type of surgery
        int idType = rgSurgeryType.getCheckedRadioButtonId();
        if (idType != -1) {
            RadioButton rb = findViewById(idType);
            String type = rb.getText().toString();
            int score = 0;

            switch (type.toLowerCase()) {
                case "thoracic": score = 7; break;
                case "upper abdominal": score = 5; break;
                case "lower abdominal": score = 3; break;
                case "neurosurgery": score = 3; break;
                case "orthopedic": score = 2; break;
                case "ent / head & neck": score = 2; break;
                case "vascular / cardiac": score = 7; break;
                case "others": score = 1; break;
            }

            if ("Others".equalsIgnoreCase(type)) {
                String otherTxt = etOtherSurgery.getText().toString().trim();
                if (!otherTxt.isEmpty()) type += " (" + otherTxt + ")";
            }

            totalScore += score;
            answers.add(new Answer("Type of surgery", type, score, "Surgery Factors"));
        }

        // Urgency
        int idUrg = rgUrgency.getCheckedRadioButtonId();
        if (idUrg != -1) {
            RadioButton rb = findViewById(idUrg);
            String urgency = rb.getText().toString();
            int score = "Emergency".equalsIgnoreCase(urgency) ? 4 : 0;
            totalScore += score;
            answers.add(new Answer("Urgency", urgency, score, "Surgery Factors"));
        }

        // Duration
        int idDur = rgDuration.getCheckedRadioButtonId();
        if (idDur != -1) {
            RadioButton rb = findViewById(idDur);
            String dur = rb.getText().toString();
            int score = dur.contains("2–4") || dur.contains("2-4") ? 3 : dur.contains(">4") ? 5 : 0;
            totalScore += score;
            answers.add(new Answer("Duration", dur, score, "Surgery Factors"));
        }

        // Estimated blood loss
        int idLoss = rgBloodLoss.getCheckedRadioButtonId();
        if (idLoss != -1) {
            RadioButton rb = findViewById(idLoss);
            String loss = rb.getText().toString();
            int score = loss.contains("500–1000") || loss.contains("500-1000") ? 2 : loss.contains(">1000") ? 3 : 0;
            totalScore += score;
            answers.add(new Answer("Estimated blood loss", loss, score, "Surgery Factors"));
        }

        if (answers.isEmpty()) {
            Toast.makeText(this, "Please answer at least one question", Toast.LENGTH_SHORT).show();
            return;
        }

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(totalScore);
        req.setStatus("surgery_Factors");
        req.setRisk_level(getRiskLevel(totalScore));
        req.setSection_scores(List.of(new SectionScore("Surgery Factors", totalScore)));
        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    startActivity(new Intent(SurgeryFactorsActivity.this, PlannedAnesthesiaActivity.class)
                            .putExtra("patient_id", patientId));
                    finish();
                } else {
                    Toast.makeText(SurgeryFactorsActivity.this, "Save failed: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(SurgeryFactorsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getRiskLevel(int score) {
        if (score <= 2) return "Low";
        else if (score <= 5) return "Moderate";
        else return "High";
    }
}
