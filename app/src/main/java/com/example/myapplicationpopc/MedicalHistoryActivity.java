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

    private int patientId;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);


        initViews();

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "⚠️ Invalid patient ID.", Toast.LENGTH_LONG).show();
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

       /*btnBack.setOnClickListener(v -> {
            Intent i = new Intent(this, PatientDemographicsActivity.class);
            i.putExtra("patient_id", patientId);
            startActivity(i);
            finish();
        });*/

        btnNext.setOnClickListener(v -> sendSurvey());
    }

    private void initViews() {
        btnNext = findViewById(R.id.btnNext);
        //btnBack = findViewById(R.id.btnBack);

        rgCOPD         = findViewById(R.id.rgCOPD);
        rgAsthma       = findViewById(R.id.rgAsthma);
        rgOSA          = findViewById(R.id.rgOSA);
        rgILD          = findViewById(R.id.rgILD);
        rgHeartFailure = findViewById(R.id.rgHeartFailure);
        rgCAD          = findViewById(R.id.rgCAD);
        rgHypertension = findViewById(R.id.rgHypertension);
        rgDiabetes     = findViewById(R.id.rgDiabetes);
        rgCKD          = findViewById(R.id.rgCKD);
    }

    private void sendSurvey() {
        if (patientId <= 0 || token == null) {
            Toast.makeText(this, "Cannot proceed without patient ID or token.", Toast.LENGTH_LONG).show();
            return;
        }

        int sectionScore = 0;
        List<Answer> answers = new ArrayList<>();

        sectionScore += addDiseaseScore(rgCOPD, "COPD", 3, answers);
        sectionScore += addDiseaseScore(rgAsthma, "Asthma", 2, answers);
        sectionScore += addDiseaseScore(rgOSA, "OSA", 2, answers);
        sectionScore += addDiseaseScore(rgILD, "ILD", 3, answers);
        sectionScore += addDiseaseScore(rgHeartFailure, "Heart Failure", 2, answers);
        sectionScore += addDiseaseScore(rgCAD, "CAD", 2, answers);
        sectionScore += addDiseaseScore(rgHypertension, "Hypertension", 1, answers);
        sectionScore += addDiseaseScore(rgDiabetes, "Diabetes", 1, answers);
        sectionScore += addDiseaseScore(rgCKD, "CKD", 2, answers);

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(sectionScore); // Only this section score
        req.setStatus("medical_history");
        req.setRisk_level(getRiskLevel(sectionScore)); // optional

        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Medical History", sectionScore));
        req.setSection_scores(sections);
        req.setAnswers(answers);

        // Optional: log JSON for debugging
        // Log.d("SurveyRequestJSON", new Gson().toJson(req));

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    goToNext();
                } else {
                    Toast.makeText(MedicalHistoryActivity.this,
                            "Save failed: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(MedicalHistoryActivity.this,
                        "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToNext() {
        Intent i = new Intent(MedicalHistoryActivity.this, PreoperativeConsiderationsActivity.class);
        i.putExtra("patient_id", patientId); // Only pass patient ID
        startActivity(i);
        finish();
    }

    private int addDiseaseScore(RadioGroup group, String label, int weight, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;

        RadioButton btn = findViewById(id);
        String ans = btn.getText().toString();
        int score = ans.equalsIgnoreCase("Yes") ? weight : 0;
        answers.add(new Answer(label, ans, score));
        return score;
    }

    private String getRiskLevel(int score) {
        if (score <= 5) return "Low";
        else if (score <= 10) return "Moderate";
        else return "High";
    }
}
