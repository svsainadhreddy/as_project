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

/**
 * Preoperative considerations survey.
 * No need to pass scores between activities.
 */
public class PreoperativeConsiderationsActivity extends AppCompatActivity {

    private RadioGroup rgAsa, rgExercise, rgDyspnea, rgInfection, rgSpO2;
    private Button btnNext;
    private ImageButton btnBack;

    private int patientId;
    private ApiService apiService;
    private String token;

    // Selection states
    private int checkedAsa = -1, checkedEx = -1, checkedDys = -1, checkedInf = -1, checkedSpO2 = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preoperative_functional_status);
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
        }

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        rgAsa = findViewById(R.id.rgAsa);
        rgExercise = findViewById(R.id.rgExercise);
        rgDyspnea = findViewById(R.id.rgDyspnea);
        rgInfection = findViewById(R.id.rgInfection);
        rgSpO2 = findViewById(R.id.rgSpO2);

        // Restore selection state
        rgAsa.check(checkedAsa);
        rgExercise.check(checkedEx);
        rgDyspnea.check(checkedDys);
        rgInfection.check(checkedInf);
        rgSpO2.check(checkedSpO2);

        // Save selections on change
        rgAsa.setOnCheckedChangeListener((g,i)-> checkedAsa=i);
        rgExercise.setOnCheckedChangeListener((g,i)-> checkedEx=i);
        rgDyspnea.setOnCheckedChangeListener((g,i)-> checkedDys=i);
        rgInfection.setOnCheckedChangeListener((g,i)-> checkedInf=i);
        rgSpO2.setOnCheckedChangeListener((g,i)-> checkedSpO2=i);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MedicalHistoryActivity.class)
                    .putExtra("patient_id", patientId));
            finish();
        });

        btnNext.setOnClickListener(v -> sendSurvey());
    }

    private void sendSurvey() {
        List<Answer> answers = new ArrayList<>();
        int totalScore = 0;

        totalScore += addAsaScore(rgAsa, answers);
        totalScore += addExerciseScore(rgExercise, answers);
        totalScore += addYesNoScore(rgDyspnea, "Dyspnea at rest", 4, answers);
        totalScore += addYesNoScore(rgInfection, "Recent respiratory infection", 3, answers);
        totalScore += addSpO2Score(rgSpO2, answers);

        if (answers.isEmpty()) {
            Toast.makeText(this, "Please answer at least one question.", Toast.LENGTH_SHORT).show();
            return;
        }

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(totalScore);
        req.setStatus("preoperative_considerations");
        req.setRisk_level(getRiskLevel(totalScore));
        req.setSection_scores(List.of(new SectionScore("Preoperative Considerations", totalScore)));
        req.setAnswers(answers);

        // Make totalScore effectively final for callback
        final int scoreToSend = totalScore;

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PreoperativeConsiderationsActivity.this,
                            "Saved successfully! Score: " + scoreToSend,
                            Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(PreoperativeConsiderationsActivity.this, SurgeryFactorsActivity.class);
                    i.putExtra("patient_id", patientId);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(PreoperativeConsiderationsActivity.this,
                            "Save failed: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(PreoperativeConsiderationsActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ----- Score helpers -----
    private int addAsaScore(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String v = rb.getText().toString();
        int s;
        switch (v) {
            case "I": s=0; break;
            case "II": s=2; break;
            case "III": s=4; break;
            case "IV": s=6; break;
            case "V": s=8; break;
            default: s=0;
        }
        answers.add(new Answer("ASA Physical Status", v, s, "Preoperative Considerations"));
        return s;
    }

    private int addExerciseScore(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        int s = (rb.getText().toString().contains("<4") || rb.getText().toString().contains("less")) ? 3 : 0;
        answers.add(new Answer("Exercise tolerance", rb.getText().toString(), s, "Preoperative Considerations"));
        return s;
    }

    private int addYesNoScore(RadioGroup group, String key, int weight, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        int s = rb.getText().toString().equalsIgnoreCase("Yes") ? weight : 0;
        answers.add(new Answer(key, rb.getText().toString(), s, "Preoperative Considerations"));
        return s;
    }

    private int addSpO2Score(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        int s;
        String v = rb.getText().toString();
        if (v.contains("96")) s=0;
        else if (v.contains("91")) s=2;
        else if (v.contains("90")) s=4;
        else s=0;
        answers.add(new Answer("SpO₂", v, s, "Preoperative Considerations"));
        return s;
    }

    private String getRiskLevel(int score) {
        if (score <= 2) return "Low";
        else if (score <= 5) return "Moderate";
        else return "High";
    }
}
