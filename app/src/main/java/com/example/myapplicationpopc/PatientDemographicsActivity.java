package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
    private int patientId = -1;
    private String token;
    private ApiService apiService;

    /** Persist selections across recreations **/
    private static int checkedAge = -1;
    private static int checkedSex = -1;
    private static int checkedBmi = -1;
    private static int checkedSmoking = -1;
    private static int checkedAlcohol = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_demographics);

        initViews();

        // Hide Toolbar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Get patient_id from intent
        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID. Please create a patient first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Get token from intent or SharedPrefManager
        String intentToken = getIntent().getStringExtra("auth_token");
        if (intentToken != null && !intentToken.isEmpty()) {
            token = intentToken;
        } else {
            String savedToken = SharedPrefManager.getInstance(this).getToken();
            if (savedToken == null || savedToken.trim().isEmpty()) {
                Toast.makeText(this, "Authentication token missing. Please login again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            token = "Token " + savedToken.trim();
        }

        apiService = ApiClient.getClient().create(ApiService.class);

        // Restore previous selections
        rgAge.check(checkedAge);
        rgSex.check(checkedSex);
        rgBmi.check(checkedBmi);
        rgSmoking.check(checkedSmoking);
        rgAlcohol.check(checkedAlcohol);

        // Save selected radio for persistence
        rgAge.setOnCheckedChangeListener((g, i) -> checkedAge = i);
        rgSex.setOnCheckedChangeListener((g, i) -> checkedSex = i);
        rgBmi.setOnCheckedChangeListener((g, i) -> checkedBmi = i);
        rgSmoking.setOnCheckedChangeListener((g, i) -> checkedSmoking = i);
        rgAlcohol.setOnCheckedChangeListener((g, i) -> checkedAlcohol = i);

        btnNext.setOnClickListener(v -> sendSurvey());
    }

    private void initViews() {
        rgAge = findViewById(R.id.rgAge);
        rgSex = findViewById(R.id.rgSex);
        rgBmi = findViewById(R.id.rgBmi);
        rgSmoking = findViewById(R.id.rgSmoking);
        rgAlcohol = findViewById(R.id.rgAlcohol);
        btnNext = findViewById(R.id.btnNext);
    }

    private void sendSurvey() {
        List<Answer> answers = new ArrayList<>();
        int totalScore = 0;

        totalScore += addScoreFromRadio(rgAge, "Age", "Patient Demographics", answers);
        addSimpleAnswer(rgSex, "Sex", "Patient Demographics", 0, answers);
        totalScore += addScoreFromRadio(rgBmi, "BMI", "Patient Demographics", answers);
        totalScore += addScoreFromRadio(rgSmoking, "Smoking", "Patient Demographics", answers);
        totalScore += addScoreFromRadio(rgAlcohol, "Alcohol", "Patient Demographics", answers);

        if (answers.isEmpty()) {
            Toast.makeText(this, "Please select at least one option.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build SurveyRequest
        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(totalScore);
        req.setStatus("patient_Demographics");
        req.setRisk_level(getRiskLevel(totalScore));
        req.setSection_scores(List.of(new SectionScore("Patient Demographics", totalScore)));
        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PatientDemographicsActivity.this, "Survey saved!", Toast.LENGTH_SHORT).show();
                    // Go to next
                    startActivity(new Intent(PatientDemographicsActivity.this, MedicalHistoryActivity.class)
                            .putExtra("patient_id", patientId));
                    finish();
                } else {
                    Toast.makeText(PatientDemographicsActivity.this,
                            "Save failed (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(PatientDemographicsActivity.this,
                        "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private int addScoreFromRadio(RadioGroup group, String label, String sectionName, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton btn = findViewById(id);
        String text = btn.getText().toString().trim().toLowerCase();
        int score = 0;

        switch (label) {
            case "Age":
                if (text.contains("<50")) score = 0;
                else if (text.contains("69")) score = 2;
                else if (text.contains("70")) score = 3;
                break;
            case "BMI":
                if (text.contains("<30")) score = 0;
                else if (text.contains(">=30")) score = 2;
                break;
            case "Smoking":
                if (text.contains("never")) score = 0;
                else if (text.contains("ex")) score = 1;
                else if (text.contains("current")) score = 2;
                break;
            case "Alcohol":
                if (text.contains("yes")) score = 1;
                break;
        }

        answers.add(new Answer(label, btn.getText().toString(), score, sectionName));
        return score;
    }

    private void addSimpleAnswer(RadioGroup group, String label, String sectionName, int score, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id != -1) {
            RadioButton btn = findViewById(id);
            answers.add(new Answer(label, btn.getText().toString(), score, sectionName));
        }
    }

    private String getRiskLevel(int score) {
        if (score <= 2) return "Low";
        else if (score <= 5) return "Moderate";
        else return "High";
    }
}
