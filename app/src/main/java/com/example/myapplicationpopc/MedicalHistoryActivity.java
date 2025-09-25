package com.example.myapplicationpopc;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class MedicalHistoryActivity extends AppCompatActivity {

    private RadioGroup rgCOPD, rgAsthma, rgOSA, rgILD, rgHeartFailure,
            rgCAD, rgHypertension, rgDiabetes, rgCKD;
    private Button btnNext;
    private ImageButton btnBack;

    private int patientId = -1;
    private int patientScore = 0;   // total from previous screen
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);

        // --- Retrieve extras ---
        Intent fromPrev = getIntent();
        patientId = fromPrev.getIntExtra("patient_id", -1);
        patientScore = fromPrev.getIntExtra("patient_score", 0);

        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
        }

        // --- Find views ---
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        rgCOPD         = findViewById(R.id.rgCOPD);
        rgAsthma       = findViewById(R.id.rgAsthma);
        rgOSA          = findViewById(R.id.rgOSA);
        rgILD          = findViewById(R.id.rgILD);
        rgHeartFailure = findViewById(R.id.rgHeartFailure);
        rgCAD          = findViewById(R.id.rgCAD);
        rgHypertension = findViewById(R.id.rgHypertension);
        rgDiabetes     = findViewById(R.id.rgDiabetes);
        rgCKD          = findViewById(R.id.rgCKD);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScoreAndSend());
    }

    private void calculateScoreAndSend() {
        if (patientId <= 0) {
            Toast.makeText(this,
                    "Cannot proceed without a valid patient ID.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        int medicalScore = 0;
        List<Answer> answers = new ArrayList<>();

        // Helper call for each disease
        medicalScore += addDiseaseScore(rgCOPD,         "COPD", 3, answers);
        medicalScore += addDiseaseScore(rgAsthma,       "Asthma", 2, answers);
        medicalScore += addDiseaseScore(rgOSA,          "Obstructive Sleep Apnea", 2, answers);
        medicalScore += addDiseaseScore(rgILD,          "Interstitial Lung Disease", 3, answers);
        medicalScore += addDiseaseScore(rgHeartFailure, "Heart Failure", 2, answers);
        medicalScore += addDiseaseScore(rgCAD,          "CAD / Recent MI", 2, answers);
        medicalScore += addDiseaseScore(rgHypertension, "Hypertension", 1, answers);
        medicalScore += addDiseaseScore(rgDiabetes,     "Diabetes", 1, answers);
        medicalScore += addDiseaseScore(rgCKD,          "Chronic Kidney Disease", 2, answers);

        if (medicalScore > 15) medicalScore = 15;

        if (answers.isEmpty()) {
            Toast.makeText(this,
                    "Please select at least one option",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final int sectionScore = medicalScore;
        final int combinedTotal = patientScore + sectionScore; // running total if needed

        // --- Build SurveyRequest ---
        SurveyRequest request = new SurveyRequest();
        request.setPatient_id(patientId);
        request.setTotal_score(sectionScore); // or combinedTotal if you want cumulative

        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Medical History", sectionScore));
        request.setSection_scores(sections);
        request.setAnswers(answers);

        // --- POST to Django ---
        apiService.createSurvey(token, request).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MedicalHistoryActivity.this,
                            "Medical history saved. Score: " + sectionScore,
                            Toast.LENGTH_SHORT).show();

                    // 👉 Move to next activity
                    Intent intent = new Intent(MedicalHistoryActivity.this,
                            PreoperativeConsiderationsActivity.class);
                    intent.putExtra("patient_id", patientId);
                    intent.putExtra("patient_score", combinedTotal);
                    intent.putExtra("medical_score", sectionScore);
                    intent.putExtra("survey_id", response.body().getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MedicalHistoryActivity.this,
                            "Save failed: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(MedicalHistoryActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private int addDiseaseScore(RadioGroup group, String key, int weight, List<Answer> answers) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == -1) return 0;

        RadioButton rb = findViewById(checkedId);
        String ans = rb.getText().toString();
        int score = ans.equalsIgnoreCase("Yes") ? weight : 0;
        answers.add(new Answer(key, ans, score));
        return score;
    }
}

