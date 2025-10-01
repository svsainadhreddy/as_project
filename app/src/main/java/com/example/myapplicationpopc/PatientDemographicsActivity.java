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
 * Collects demographic survey answers and posts them to the Django backend.
 * Keeps user selections when navigating back from the next screen.
 */
public class PatientDemographicsActivity extends AppCompatActivity {

    private RadioGroup rgAge, rgSex, rgBmi, rgSmoking, rgAlcohol;
    private Button btnNext;
    private ImageButton btnBack;

    private int patientId = -1;
    private String token;
    private ApiService apiService;

    /** ---------- Static selections to persist across Activity recreations ---------- **/
    private static int checkedAge     = -1;
    private static int checkedSex     = -1;
    private static int checkedBmi     = -1;
    private static int checkedSmoking = -1;
    private static int checkedAlcohol = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_demographics);

        initViews();

        patientId = getIntent().getIntExtra("patient_id", -1);
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

        // Restore previously checked options
        rgAge.check(checkedAge);
        rgSex.check(checkedSex);
        rgBmi.check(checkedBmi);
        rgSmoking.check(checkedSmoking);
        rgAlcohol.check(checkedAlcohol);

        // Update static variables whenever a selection changes
        rgAge.setOnCheckedChangeListener((g, i) -> checkedAge = i);
        rgSex.setOnCheckedChangeListener((g, i) -> checkedSex = i);
        rgBmi.setOnCheckedChangeListener((g, i) -> checkedBmi = i);
        rgSmoking.setOnCheckedChangeListener((g, i) -> checkedSmoking = i);
        rgAlcohol.setOnCheckedChangeListener((g, i) -> checkedAlcohol = i);

        btnBack.setOnClickListener(v ->
                startActivity(new Intent(this, PatientManagementActivity.class)));

        btnNext.setOnClickListener(v -> sendSurvey());
    }

    private void initViews() {
        rgAge     = findViewById(R.id.rgAge);
        rgSex     = findViewById(R.id.rgSex);
        rgBmi     = findViewById(R.id.rgBmi);
        rgSmoking = findViewById(R.id.rgSmoking);
        rgAlcohol = findViewById(R.id.rgAlcohol);
        btnNext   = findViewById(R.id.btnNext);
        btnBack   = findViewById(R.id.btnBack);
    }

    private void sendSurvey() {
        if (patientId <= 0 || token == null) {
            Toast.makeText(this,
                    "Cannot proceed without patient ID or token.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        /** Use array so reference is final but value is mutable **/
        final int[] demoScore = {0};
        List<Answer> answers = new ArrayList<>();

        demoScore[0] += addAgeScore(answers);
        addSimpleAnswer(rgSex, "Sex", 0, answers);
        demoScore[0] += addBmiScore(answers);
        demoScore[0] += addSmokingScore(answers);
        demoScore[0] += addAlcoholScore(answers);

        if (answers.isEmpty()) {
            Toast.makeText(this,
                    "Please select at least one option.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(demoScore[0]);

        // ✅ Add status + risk level
        req.setStatus("patient_Demographics");
        req.setRisk_level(getRiskLevel(demoScore[0]));

        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Patient Demographics", demoScore[0]));
        req.setSection_scores(sections);
        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call,
                                   Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    goToNext(demoScore[0]);   // ✅ safe to access demoScore[0]
                } else {
                    Toast.makeText(PatientDemographicsActivity.this,
                            "Save failed (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(PatientDemographicsActivity.this,
                        "Network error: " + t.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToNext(int score) {
        Intent i = new Intent(PatientDemographicsActivity.this,
                MedicalHistoryActivity.class);
        i.putExtra("patient_id", patientId);
        i.putExtra("patient_score", score);
        startActivity(i);
        finish();
    }

    // ---------- Scoring helpers ----------
    private int addAgeScore(List<Answer> answers) { return addScoreFromRadio(rgAge, "Age", answers); }
    private int addBmiScore(List<Answer> answers) { return addScoreFromRadio(rgBmi, "BMI", answers); }
    private int addSmokingScore(List<Answer> answers) { return addScoreFromRadio(rgSmoking, "Smoking", answers); }
    private int addAlcoholScore(List<Answer> answers) { return addScoreFromRadio(rgAlcohol, "Alcohol", answers); }

    private int addScoreFromRadio(RadioGroup group, String label,
                                  List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;

        RadioButton btn = findViewById(id);
        String text = btn.getText().toString().trim().toLowerCase();
        int score = 0;

        switch (label) {
            case "Age":
                if (text.contains("<50")) score = 0;
                else if (text.contains("50")) score = 2;
                else if (text.contains("70")) score = 3;
                break;
            case "BMI":
                if (text.contains("<30")) score = 0;
                else if (text.contains(">30")) score = 2;
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

        answers.add(new Answer(label, btn.getText().toString(), score));
        return score;
    }

    private void addSimpleAnswer(RadioGroup group, String label,
                                 int score, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        if (id != -1) {
            RadioButton btn = findViewById(id);
            answers.add(new Answer(label, btn.getText().toString(), score));
        }
    }

    // ---------- Risk level calculator ----------
    private String getRiskLevel(int score) {
        if (score <= 2) return "Low";
        else if (score <= 5) return "Moderate";
        else return "High";
    }
}
