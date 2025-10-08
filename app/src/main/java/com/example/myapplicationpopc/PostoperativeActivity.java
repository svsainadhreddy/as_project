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

    private int patientScore, medicalScore, preopScore, surgeryScore, plannedAnesthesiaScore;
    private int patientId = -1;
    private int postopScore;

    private String token;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postoperative);
        // Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();

        patientScore           = getIntent().getIntExtra("patient_score", 0);
        medicalScore           = getIntent().getIntExtra("medical_score", 0);
        preopScore             = getIntent().getIntExtra("preop_score", 0);
        surgeryScore           = getIntent().getIntExtra("surgery_score", 0);
        plannedAnesthesiaScore = getIntent().getIntExtra("anesthetic_score", 0);
        patientId              = getIntent().getIntExtra("patient_id", -1);

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

        btnBack.setOnClickListener(v -> finish()); // just go back

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

        postopScore = 0;
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
                    // ✅ Move to ScoreActivity after successful save
                    Intent intent = new Intent(PostoperativeActivity.this, ScoreActivity.class);
                    intent.putExtra("patient_id", patientId); // only send patient_id
                    startActivity(intent);
                    finish(); // close PostoperativeActivity
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

    private int addIcuAnswer(List<Answer> answers) {
        int id = groupIcu.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        int score = rb.getText().toString().equalsIgnoreCase(getString(R.string.yes)) ? 2 : 0;
        answers.add(new Answer("Planned ICU/HDU admission", rb.getText().toString(), score));
        return score;
    }

    private int addVentilationAnswer(List<Answer> answers) {
        int id = groupVentilation.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        int score = rb.getText().toString().equalsIgnoreCase(getString(R.string.yes)) ? 4 : 0;
        answers.add(new Answer("Anticipated >24h ventilation", rb.getText().toString(), score));
        return score;
    }

    private int addAnalgesiaAnswer(List<Answer> answers) {
        int id = groupAnalgesia.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        int score = rb.getText().toString().equalsIgnoreCase(getString(R.string.opioid_heavy)) ? 2 : 0;
        answers.add(new Answer("Post-op analgesia", rb.getText().toString(), score));
        return score;
    }

    private int addMobilizationAnswer(List<Answer> answers) {
        int id = groupMobilization.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        int score = rb.getText().toString().equalsIgnoreCase(getString(R.string.no)) ? 2 : 0;
        answers.add(new Answer("Early mobilization within 24h", rb.getText().toString(), score));
        return score;
    }

    private String getRiskLevel(int score) {
        if (score <= 20) return "Low";
        else if (score <= 40) return "Moderate";
        else if (score <= 60) return "High";
        else return "Very high";
    }
}
