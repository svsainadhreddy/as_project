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

public class PreoperativeConsiderationsActivity extends AppCompatActivity {

    private RadioGroup rgAsa, rgExercise, rgDyspnea, rgInfection, rgSpO2;
    private Button btnNext;
    private ImageButton btnBack;

    // 👉 scores received from previous activities
    private int patientScore = 0;
    private int medicalScore = 0;
    private int patientId = -1;

    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preoperative_functional_status);

        // --- receive previous scores ---
        Intent fromPrev = getIntent();
        patientScore = fromPrev.getIntExtra("patient_score", 0);
        medicalScore = fromPrev.getIntExtra("medical_score", 0);
        patientId = fromPrev.getIntExtra("patient_id", -1);

        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        // --- Bind Views ---
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        rgAsa = findViewById(R.id.rgAsa);
        rgExercise = findViewById(R.id.rgExercise);
        rgDyspnea = findViewById(R.id.rgDyspnea);
        rgInfection = findViewById(R.id.rgInfection);
        rgSpO2 = findViewById(R.id.rgSpO2);

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScoreAndSend());
    }

    private void calculateScoreAndSend() {
        int tmpScore = 0;
        List<Answer> answers = new ArrayList<>();

        tmpScore += addAsaScore(rgAsa, answers);
        tmpScore += addExerciseScore(rgExercise, answers);
        tmpScore += addYesNoScore(rgDyspnea, "Dyspnea at rest", 4, answers);
        tmpScore += addYesNoScore(rgInfection, "Recent respiratory infection", 3, answers);
        tmpScore += addSpO2Score(rgSpO2, answers);

        if (answers.isEmpty()) {
            Toast.makeText(this,
                    "Please answer at least one question",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final int preopScore = tmpScore;
        final int combinedTotal = patientScore + medicalScore + preopScore;

        // --- Build SurveyRequest ---
        SurveyRequest request = new SurveyRequest();
        request.setPatient_id(patientId);
        request.setTotal_score(preopScore); // only this section

        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Preoperative Considerations", preopScore));
        request.setSection_scores(sections);
        request.setAnswers(answers);

        // --- POST to Django ---
        apiService.createSurvey(token, request).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PreoperativeConsiderationsActivity.this,
                            "Preoperative data saved. Score: " + preopScore,
                            Toast.LENGTH_SHORT).show();

                    // 👉 Move to next activity
                    Intent intent = new Intent(PreoperativeConsiderationsActivity.this,
                            SurgeryFactorsActivity.class);
                    intent.putExtra("patient_id", patientId);
                    intent.putExtra("patient_score", patientScore);
                    intent.putExtra("medical_score", medicalScore);
                    intent.putExtra("preop_score", preopScore);
                    intent.putExtra("combined_total", combinedTotal);
                    intent.putExtra("survey_id", response.body().getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(PreoperativeConsiderationsActivity.this,
                            "Save failed: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(PreoperativeConsiderationsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private int addAsaScore(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String value = rb.getText().toString();
        int score;
        switch (value) {
            case "I":  score = 0; break;
            case "II": score = 2; break;
            case "III":score = 4; break;
            case "IV": score = 6; break;
            case "V":  score = 8; break;
            default:   score = 0;
        }
        answers.add(new Answer("ASA Physical Status", value, score));
        return score;
    }

    private int addExerciseScore(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String value = rb.getText().toString();
        int score = (value.contains("<4") || value.contains("less")) ? 3 : 0;
        answers.add(new Answer("Exercise tolerance", value, score));
        return score;
    }

    private int addYesNoScore(RadioGroup group, String key, int weight, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String value = rb.getText().toString();
        int score = value.equalsIgnoreCase("Yes") ? weight : 0;
        answers.add(new Answer(key, value, score));
        return score;
    }

    private int addSpO2Score(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String value = rb.getText().toString();
        int score;
        if (value.contains("96"))      score = 0;
        else if (value.contains("91")) score = 2;
        else if (value.contains("90")) score = 4;
        else                            score = 0;
        answers.add(new Answer("SpO₂", value, score));
        return score;
    }
}
