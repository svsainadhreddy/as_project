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

public class PatientDemographicsActivity extends AppCompatActivity {

    private RadioGroup rgAge, rgSex, rgBmi, rgSmoking, rgAlcohol;
    private Button btnNext;
    private ImageButton btnBack;

    private int patientId = -1;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_demographics);

        rgAge     = findViewById(R.id.rgAge);
        rgSex     = findViewById(R.id.rgSex);
        rgBmi     = findViewById(R.id.rgBmi);
        rgSmoking = findViewById(R.id.rgSmoking);
        rgAlcohol = findViewById(R.id.rgAlcohol);
        btnNext   = findViewById(R.id.btnNext);
        btnBack   = findViewById(R.id.btnBack);

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
        }

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

        int demoScore = 0;
        List<Answer> answers = new ArrayList<>();

        // ---- Age ----
        demoScore += addAgeScore(answers);

        // ---- Sex ----
        addSimpleAnswer(rgSex, "Sex", 0, answers);

        // ---- BMI ----
        demoScore += addBmiScore(answers);

        // ---- Smoking ----
        demoScore += addSmokingScore(answers);

        // ---- Alcohol ----
        demoScore += addAlcoholScore(answers);

        if (answers.isEmpty()) {
            Toast.makeText(this,
                    "Please select at least one option",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final int sectionScore = demoScore; // score for this section

        // Build survey request
        SurveyRequest request = new SurveyRequest();
        request.setPatient_id(patientId);
        request.setTotal_score(sectionScore);  // just this section

        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Patient Demographics", sectionScore));
        request.setSection_scores(sections);
        request.setAnswers(answers);

        // POST to Django
        apiService.createSurvey(token, request).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PatientDemographicsActivity.this,
                            "Demographics saved. Score: " + sectionScore,
                            Toast.LENGTH_SHORT).show();

                    // 👉 Go to MedicalHistoryActivity
                    Intent intent = new Intent(PatientDemographicsActivity.this,
                            MedicalHistoryActivity.class);
                    intent.putExtra("patient_id", patientId);
                    intent.putExtra("patient_score", sectionScore); // pass forward
                    intent.putExtra("survey_id", response.body().getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(PatientDemographicsActivity.this,
                            "Save failed: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(PatientDemographicsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ---- Helper scoring methods ----
    private int addAgeScore(List<Answer> answers) {
        int id = rgAge.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton btn = findViewById(id);
        String text = btn.getText().toString();
        int score = (text.contains("<50")) ? 0 :
                (text.contains("50")) ? 2 :
                        (text.contains("70") || text.contains("≥70")) ? 3 : 0;
        answers.add(new Answer("Age", text, score));
        return score;
    }

    private int addBmiScore(List<Answer> answers) {
        int id = rgBmi.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton btn = findViewById(id);
        String text = btn.getText().toString();
        int score = (text.contains("<30")) ? 0 :
                (text.contains("≥30") || text.contains("30")) ? 2 : 0;
        answers.add(new Answer("BMI", text, score));
        return score;
    }

    private int addSmokingScore(List<Answer> answers) {
        int id = rgSmoking.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton btn = findViewById(id);
        String text = btn.getText().toString();
        int score = (text.toLowerCase().contains("never")) ? 0 :
                (text.toLowerCase().contains("ex")) ? 1 :
                        (text.toLowerCase().contains("current")) ? 2 : 0;
        answers.add(new Answer("Smoking status", text, score));
        return score;
    }

    private int addAlcoholScore(List<Answer> answers) {
        int id = rgAlcohol.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton btn = findViewById(id);
        String text = btn.getText().toString();
        int score = (text.toLowerCase().contains("yes")) ? 1 : 0;
        answers.add(new Answer("Alcohol consumption", text, score));
        return score;
    }

    private void addSimpleAnswer(RadioGroup group, String label, int defaultScore,
                                 List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return;
        RadioButton btn = findViewById(id);
        answers.add(new Answer(label, btn.getText().toString(), defaultScore));
    }
}
