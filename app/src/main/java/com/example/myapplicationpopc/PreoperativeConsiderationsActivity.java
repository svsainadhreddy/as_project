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
 * Collects preoperative survey answers and posts them to Django backend.
 * Does NOT leak selections across patients.
 */
public class PreoperativeConsiderationsActivity extends AppCompatActivity {

    private RadioGroup rgAsa, rgExercise, rgDyspnea, rgInfection, rgSpO2;
    private Button btnNext;
    private ImageButton btnBack;

    private int patientScore;   // scores received from previous screens
    private int medicalScore;
    private int patientId;

    private ApiService apiService;
    private String token;

    // Selection states (per activity instance, not static/global)
    private int checkedAsa   = -1;
    private int checkedEx    = -1;
    private int checkedDys   = -1;
    private int checkedInf   = -1;
    private int checkedSpO2  = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preoperative_functional_status);

        Intent fromPrev = getIntent();
        patientScore = fromPrev.getIntExtra("patient_score", 0);
        medicalScore = fromPrev.getIntExtra("medical_score", 0);
        patientId    = fromPrev.getIntExtra("patient_id", -1);

        // restore selection state (if coming back)
        checkedAsa  = fromPrev.getIntExtra("checkedAsa", -1);
        checkedEx   = fromPrev.getIntExtra("checkedEx", -1);
        checkedDys  = fromPrev.getIntExtra("checkedDys", -1);
        checkedInf  = fromPrev.getIntExtra("checkedInf", -1);
        checkedSpO2 = fromPrev.getIntExtra("checkedSpO2", -1);

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

        btnBack    = findViewById(R.id.btnBack);
        btnNext    = findViewById(R.id.btnNext);
        rgAsa      = findViewById(R.id.rgAsa);
        rgExercise = findViewById(R.id.rgExercise);
        rgDyspnea  = findViewById(R.id.rgDyspnea);
        rgInfection= findViewById(R.id.rgInfection);
        rgSpO2     = findViewById(R.id.rgSpO2);

        // Restore selections
        rgAsa.check(checkedAsa);
        rgExercise.check(checkedEx);
        rgDyspnea.check(checkedDys);
        rgInfection.check(checkedInf);
        rgSpO2.check(checkedSpO2);

        // Remember current selections
        rgAsa.setOnCheckedChangeListener((g,i)-> checkedAsa=i);
        rgExercise.setOnCheckedChangeListener((g,i)-> checkedEx=i);
        rgDyspnea.setOnCheckedChangeListener((g,i)-> checkedDys=i);
        rgInfection.setOnCheckedChangeListener((g,i)-> checkedInf=i);
        rgSpO2.setOnCheckedChangeListener((g,i)-> checkedSpO2=i);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MedicalHistoryActivity.class);
            intent.putExtra("patient_id", patientId);
            intent.putExtra("patient_score", patientScore);
            intent.putExtra("medical_score", medicalScore);

            // persist state for THIS patient
            intent.putExtra("checkedAsa", checkedAsa);
            intent.putExtra("checkedEx", checkedEx);
            intent.putExtra("checkedDys", checkedDys);
            intent.putExtra("checkedInf", checkedInf);
            intent.putExtra("checkedSpO2", checkedSpO2);

            startActivity(intent);
            finish();
        });

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
            Toast.makeText(this, "Please answer at least one question", Toast.LENGTH_SHORT).show();
            return;
        }
        final int preopScore    = tmpScore;
        final int combinedTotal = patientScore + medicalScore + preopScore;

        SurveyRequest request = new SurveyRequest();
        request.setPatient_id(patientId);
        request.setTotal_score(preopScore);

        // ✅ Add status + risk level
        request.setStatus("preoperative_considerations");
        request.setRisk_level(getRiskLevel(preopScore));

        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Preoperative Considerations", preopScore));
        request.setSection_scores(sections);
        request.setAnswers(answers);

        apiService.createSurvey(token, request).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PreoperativeConsiderationsActivity.this,
                            "Preoperative data saved. Score: " + preopScore,
                            Toast.LENGTH_SHORT).show();

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
        String v = rb.getText().toString();
        int s;
        switch (v) {
            case "I":  s=0; break;
            case "II": s=2; break;
            case "III":s=4; break;
            case "IV": s=6; break;
            case "V":  s=8; break;
            default:   s=0;
        }
        answers.add(new Answer("ASA Physical Status", v, s));
        return s;
    }

    private int addExerciseScore(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String v = rb.getText().toString();
        int s = (v.contains("<4") || v.contains("less")) ? 3 : 0;
        answers.add(new Answer("Exercise tolerance", v, s));
        return s;
    }

    private int addYesNoScore(RadioGroup group, String key, int weight, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String v = rb.getText().toString();
        int s = v.equalsIgnoreCase("Yes") ? weight : 0;
        answers.add(new Answer(key, v, s));
        return s;
    }

    private int addSpO2Score(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String v = rb.getText().toString();
        int s;
        if (v.contains("96"))      s = 0;
        else if (v.contains("91")) s = 2;
        else if (v.contains("90")) s = 4;
        else                        s = 0;
        answers.add(new Answer("SpO₂", v, s));
        return s;
    }

    // ---------- Risk level calculator ----------
    private String getRiskLevel(int score) {
        if (score <= 2) return "Low";
        else if (score <= 5) return "Moderate";
        else return "High";
    }
}
