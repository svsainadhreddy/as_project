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

public class PostoperativeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnNext;
    private RadioGroup groupIcu, groupAnalgesia, groupVentilation, groupMobilization;

    private int patientScore;
    private int medicalScore;
    private int preopScore;
    private int surgeryScore;
    private int plannedAnesthesiaScore;
    private int patientId = -1;

    private int postopScore;  // ✅ make field so inner class can access

    private String token;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postoperative);

        initViews();

        Intent in = getIntent();
        patientScore           = in.getIntExtra("patient_score", 0);
        medicalScore           = in.getIntExtra("medical_score", 0);
        preopScore             = in.getIntExtra("preop_score", 0);
        surgeryScore           = in.getIntExtra("surgery_score", 0);
        plannedAnesthesiaScore = in.getIntExtra("anesthetic_score", 0);
        patientId              = in.getIntExtra("patient_id", -1);

        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        String savedToken = SharedPrefManager.getInstance(this).getToken();
        if (savedToken != null && !savedToken.trim().isEmpty()) {
            token = "Token " + savedToken.trim();
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(PostoperativeActivity.this, PlannedAnesthesiaActivity.class);
            startActivity(intent);
        });

        btnNext.setOnClickListener(v -> sendSurvey());
    }

    private void initViews() {
        btnBack           = findViewById(R.id.btnBack);
        btnNext           = findViewById(R.id.btnNext);
        groupIcu          = findViewById(R.id.groupIcu);
        groupAnalgesia    = findViewById(R.id.groupAnalgesia);
        groupVentilation  = findViewById(R.id.groupVentilation);
        groupMobilization = findViewById(R.id.groupMobilization);
    }

    private void sendSurvey() {
        if (patientId <= 0 || token == null) {
            Toast.makeText(this,
                    "Cannot proceed without patient ID or token.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        postopScore = 0; // reset field
        List<Answer> answers = new ArrayList<>();

        postopScore += addIcuAnswer(answers);
        postopScore += addVentilationAnswer(answers);
        postopScore += addAnalgesiaAnswer(answers);
        postopScore += addMobilizationAnswer(answers);

        int totalScore = patientScore + medicalScore + preopScore +
                surgeryScore + plannedAnesthesiaScore + postopScore;

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(totalScore);
        req.setStatus("postoperative");
        req.setRisk_level(getRiskLevel(totalScore));

        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Postoperative", postopScore));
        req.setSection_scores(sections);
        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    goToNext(totalScore);
                } else {
                    Toast.makeText(PostoperativeActivity.this,
                            "⚠️ Failed to save (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(PostoperativeActivity.this,
                        "Network error: " + t.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToNext(int totalScore) {
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("patient_score", patientScore);
        intent.putExtra("medical_score", medicalScore);
        intent.putExtra("preop_score", preopScore);
        intent.putExtra("surgery_score", surgeryScore);
        intent.putExtra("anesthetic_score", plannedAnesthesiaScore);
        intent.putExtra("postop_score", postopScore);
        intent.putExtra("total_score", totalScore);
        startActivity(intent);
    }

    // ------------------ Scoring Helpers ------------------
    private int addIcuAnswer(List<Answer> answers) {
        int id = groupIcu.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String choice = rb.getText().toString();
        int score = choice.equalsIgnoreCase(getString(R.string.yes)) ? 2 : 0;
        answers.add(new Answer("Planned ICU/HDU admission", choice, score));
        return score;
    }

    private int addVentilationAnswer(List<Answer> answers) {
        int id = groupVentilation.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String choice = rb.getText().toString();
        int score = choice.equalsIgnoreCase(getString(R.string.yes)) ? 4 : 0;
        answers.add(new Answer("Anticipated >24h ventilation", choice, score));
        return score;
    }

    private int addAnalgesiaAnswer(List<Answer> answers) {
        int id = groupAnalgesia.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String choice = rb.getText().toString();
        int score = choice.equalsIgnoreCase(getString(R.string.opioid_heavy)) ? 2 : 0;
        answers.add(new Answer("Post-op analgesia", choice, score));
        return score;
    }

    private int addMobilizationAnswer(List<Answer> answers) {
        int id = groupMobilization.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String choice = rb.getText().toString();
        int score = choice.equalsIgnoreCase(getString(R.string.no)) ? 2 : 0;
        answers.add(new Answer("Early mobilization within 24h", choice, score));
        return score;
    }

    // ------------------ Risk Level Calculator ------------------
    private String getRiskLevel(int score) {
        if (score <= 2) return "Low";
        else if (score <= 5) return "Moderate";
        else return "High";
    }
}
