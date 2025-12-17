package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.popc.model.SurveyRequest;
import com.simats.popc.model.SurveyRequest.Answer;
import com.simats.popc.model.SurveyRequest.SectionScore;
import com.simats.popc.model.SurveyResponse;
import com.simats.popc.model.SurveyAnswersResponse;
import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

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

    // Persist UI state across screens
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
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String intentToken = getIntent().getStringExtra("auth_token");
        if (intentToken != null) {
            token = intentToken;
        } else {
            String savedToken = SharedPrefManager.getInstance(this).getToken();
            if (savedToken == null) {
                Toast.makeText(this, "Login required.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            token = "Token " + savedToken;
        }

        apiService = ApiClient.getClient().create(ApiService.class);

        fetchSavedSelections();
        restoreLocalSelections();

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

    // ------------------------- FETCH ANSWERS --------------------------
    private void fetchSavedSelections() {
        apiService.getSectionAnswers(token, patientId, "Patient Demographics")
                .enqueue(new Callback<SurveyAnswersResponse>() {
                    @Override
                    public void onResponse(Call<SurveyAnswersResponse> call,
                                           Response<SurveyAnswersResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            applySavedSelections(response.body().answers);
                        }
                    }

                    @Override
                    public void onFailure(Call<SurveyAnswersResponse> call, Throwable t) {
                        Toast.makeText(PatientDemographicsActivity.this,
                                "Failed to fetch saved answers.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applySavedSelections(List<SurveyAnswersResponse.AnswerItem> list) {
        if (list == null) return;

        for (SurveyAnswersResponse.AnswerItem item : list) {
            switch (item.question) {
                case "Age":
                    checkRadioGroup(rgAge, item.selected_option);
                    break;
                case "Sex":
                    checkRadioGroup(rgSex, item.selected_option);
                    break;
                case "BMI":
                    checkRadioGroup(rgBmi, item.selected_option);
                    break;
                case "Smoking":
                    checkRadioGroup(rgSmoking, item.selected_option);
                    break;
                case "Alcohol":
                    checkRadioGroup(rgAlcohol, item.selected_option);
                    break;
            }
        }
    }

    private void restoreLocalSelections() {
        if (checkedAge != -1) rgAge.check(checkedAge);
        if (checkedSex != -1) rgSex.check(checkedSex);
        if (checkedBmi != -1) rgBmi.check(checkedBmi);
        if (checkedSmoking != -1) rgSmoking.check(checkedSmoking);
        if (checkedAlcohol != -1) rgAlcohol.check(checkedAlcohol);
    }

    private void checkRadioGroup(RadioGroup group, String text) {
        if (text == null) return;
        text = text.trim().toLowerCase();

        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton rb = (RadioButton) group.getChildAt(i);
            if (rb.getText().toString().trim().toLowerCase().equals(text)) {

                group.check(rb.getId());

                if (group == rgAge) checkedAge = rb.getId();
                if (group == rgSex) checkedSex = rb.getId();
                if (group == rgBmi) checkedBmi = rb.getId();
                if (group == rgSmoking) checkedSmoking = rb.getId();
                if (group == rgAlcohol) checkedAlcohol = rb.getId();

                break;
            }
        }
    }

    // ------------------------- SEND SURVEY --------------------------
    private void sendSurvey() {

        // ************* REQUIRED FIELDS *************
        if (rgAge.getCheckedRadioButtonId() == -1 ||
                rgSex.getCheckedRadioButtonId() == -1 ||
                rgBmi.getCheckedRadioButtonId() == -1 ||
                rgSmoking.getCheckedRadioButtonId() == -1 ||
                rgAlcohol.getCheckedRadioButtonId() == -1) {

            Toast.makeText(this, "Please answer all questions.", Toast.LENGTH_LONG).show();
            return;
        }
        // *******************************************

        List<Answer> answers = new ArrayList<>();
        int totalScore = 0;

        totalScore += addScoreFromRadio(rgAge, "Age", "Patient Demographics", answers);
        addSimpleAnswer(rgSex, "Sex", "Patient Demographics", 0, answers);
        totalScore += addScoreFromRadio(rgBmi, "BMI", "Patient Demographics", answers);
        totalScore += addScoreFromRadio(rgSmoking, "Smoking", "Patient Demographics", answers);
        totalScore += addScoreFromRadio(rgAlcohol, "Alcohol", "Patient Demographics", answers);

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(totalScore);
        req.setStatus("patient_Demographics");
        req.setRisk_level(getRiskLevel(totalScore));

        List<SectionScore> sectionList = new ArrayList<>();
        sectionList.add(new SectionScore("Patient Demographics", totalScore));
        req.setSection_scores(sectionList);

        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful()) {
                    startActivity(
                            new Intent(PatientDemographicsActivity.this, MedicalHistoryActivity.class)
                                    .putExtra("patient_id", patientId)
                    );
                    finish();
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

    // ------------------------- SCORE LOGIC --------------------------
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
        if (score <= 5) return "Moderate";
        return "High";
    }
}
