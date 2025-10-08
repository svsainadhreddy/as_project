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

    // Persist selections across Activity recreation
    private static int checkedType = -1, checkedUrg = -1, checkedDur = -1, checkedLoss = -1;
    private static String otherText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgery_factors);

        initViews();
        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        String savedToken = SharedPrefManager.getInstance(this).getToken();
        if (savedToken != null && !savedToken.trim().isEmpty()) {
            token = "Token " + savedToken.trim();
        } else {
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
        if (patientId <= 0 || token == null) return;

        List<Answer> answers = new ArrayList<>();
        int totalScore = 0;

        // Type of surgery
        int idType = rgSurgeryType.getCheckedRadioButtonId();
        if (idType != -1) {
            RadioButton rb = findViewById(idType);
            String type = rb.getText().toString();
            int score = 0;
            if ("Thoracic".equalsIgnoreCase(type)) score = 7;
            else if ("Upper abdominal".equalsIgnoreCase(type)) score = 5;
            else if ("Lower abdominal".equalsIgnoreCase(type)) score = 3;
            else if ("Neurosurgery".equalsIgnoreCase(type)) score = 3;
            else if ("Orthopedic".equalsIgnoreCase(type)) score = 2;
            else if ("Ent / Head & neck".equalsIgnoreCase(type)) score = 2;
            else if ("Vascular / Cardiac".equalsIgnoreCase(type)) score = 7;
            else if ("Others".equalsIgnoreCase(type)) score = 1;

            if ("Others".equalsIgnoreCase(type)) {
                String otherTxt = etOtherSurgery.getText().toString().trim();
                if (!otherTxt.isEmpty()) type += " (" + otherTxt + ")";
            }

            totalScore += score;
            answers.add(new Answer("Type of surgery", type, score));
        }

        // Urgency
        int idUrg = rgUrgency.getCheckedRadioButtonId();
        if (idUrg != -1) {
            RadioButton rb = findViewById(idUrg);
            String urgency = rb.getText().toString();
            int score = "Emergency".equalsIgnoreCase(urgency) ? 4 : 0;
            totalScore += score;
            answers.add(new Answer("Urgency", urgency, score));
        }

        // Duration
        int idDur = rgDuration.getCheckedRadioButtonId();
        if (idDur != -1) {
            RadioButton rb = findViewById(idDur);
            String dur = rb.getText().toString();
            int score = 0;
            if (dur.contains("2–4") || dur.contains("2-4")) score = 3;
            else if (dur.contains(">4") || dur.toLowerCase().contains("gt")) score = 5;
            totalScore += score;
            answers.add(new Answer("Duration", dur, score));
        }

        // Estimated blood loss
        int idLoss = rgBloodLoss.getCheckedRadioButtonId();
        if (idLoss != -1) {
            RadioButton rb = findViewById(idLoss);
            String loss = rb.getText().toString();
            int score = 0;
            if (loss.contains("500–1000") || loss.contains("500-1000")) score = 2;
            else if (loss.contains(">1000") || loss.toLowerCase().contains("gt")) score = 3;
            totalScore += score;
            answers.add(new Answer("Estimated blood loss", loss, score));
        }

        if (answers.isEmpty()) {
            Toast.makeText(this, "Please answer at least one question", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create request
        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(totalScore);
        req.setStatus("surgery_Factors");  // match backend section
        req.setRisk_level(getRiskLevel(totalScore));
        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Surgery Factors", totalScore));
        req.setSection_scores(sections);
        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SurgeryFactorsActivity.this, "Survey saved!", Toast.LENGTH_SHORT).show();
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
