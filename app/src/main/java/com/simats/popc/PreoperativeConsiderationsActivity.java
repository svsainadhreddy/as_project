package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.popc.model.SurveyAnswersResponse;
import com.simats.popc.model.SurveyRequest;
import com.simats.popc.model.SurveyRequest.Answer;
import com.simats.popc.model.SurveyRequest.SectionScore;
import com.simats.popc.model.SurveyResponse;
import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreoperativeConsiderationsActivity extends AppCompatActivity {

    private RadioGroup rgAsa, rgExercise, rgDyspnea, rgInfection, rgSpO2;
    private Button btnNext;
    private ImageButton btnBack;

    private int patientId;
    private ApiService apiService;
    private String token;

    /** Persist UI selections */
    private static int checkedAsa = -1, checkedEx = -1, checkedDys = -1, checkedInf = -1, checkedSpO2 = -1;

    // NEW: track last patient id so selections are cleared when a different patient opens the screen
    private static int lastPatientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preoperative_functional_status);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        loadPatientId();
        loadToken();

        // If switched patient, clear previously persisted static selections
        if (lastPatientId != patientId) {
            clearStaticSelections();
            lastPatientId = patientId;
        }

        apiService = ApiClient.getClient().create(ApiService.class);

        fetchSavedSelections();
        restoreLocalSelections();

        setListeners();

        btnNext.setOnClickListener(v -> {
            if (!validateAllFields()) return;   // **Mandatory validation**
            sendSurvey();
        });
    }

    private void initViews() {
        rgAsa = findViewById(R.id.rgAsa);
        rgExercise = findViewById(R.id.rgExercise);
        rgDyspnea = findViewById(R.id.rgDyspnea);
        rgInfection = findViewById(R.id.rgInfection);
        rgSpO2 = findViewById(R.id.rgSpO2);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadPatientId() {
        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadToken() {
        String savedToken = SharedPrefManager.getInstance(this).getToken();
        if (savedToken == null || savedToken.trim().isEmpty()) {
            Toast.makeText(this, "Token missing. Login again.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            token = "Token " + savedToken.trim();
        }
    }

    /** -----------------------------------------------------------
     * VALIDATION – USER MUST FILL ALL FIELDS
     * ----------------------------------------------------------- */
    private boolean validateAllFields() {

        if (rgAsa.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select ASA Physical Status", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgExercise.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select Exercise Tolerance", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgDyspnea.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select Dyspnea at rest", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgInfection.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select Recent respiratory infection", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgSpO2.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select SpO₂", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /** -----------------------------------------------------------
     * FETCH SAVED ANSWERS
     * ----------------------------------------------------------- */
    private void fetchSavedSelections() {
        apiService.getSectionAnswers(
                token,
                patientId,
                "Preoperative Considerations"
        ).enqueue(new Callback<SurveyAnswersResponse>() {
            @Override
            public void onResponse(Call<SurveyAnswersResponse> call,
                                   Response<SurveyAnswersResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    List<SurveyAnswersResponse.AnswerItem> answers = response.body().answers;
                    if (answers != null && !answers.isEmpty()) {
                        applySavedSelections(answers);
                    }
                    // else: no saved answers for this patient — keep form blank/local selections
                }
            }

            @Override
            public void onFailure(Call<SurveyAnswersResponse> call, Throwable t) {
                Toast.makeText(PreoperativeConsiderationsActivity.this,
                        "Failed to fetch saved answers.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applySavedSelections(List<SurveyAnswersResponse.AnswerItem> list) {
        if (list == null || list.isEmpty()) return;

        for (SurveyAnswersResponse.AnswerItem item : list) {

            String q = item.question;

            if (q.equals("ASA Physical Status"))
                checkRadioGroup(rgAsa, item.selected_option);

            else if (q.equals("Exercise tolerance"))
                checkRadioGroup(rgExercise, item.selected_option);

            else if (q.equals("Dyspnea at rest"))
                checkRadioGroup(rgDyspnea, item.selected_option);

            else if (q.equals("Recent respiratory infection"))
                checkRadioGroup(rgInfection, item.selected_option);

            else if (q.equals("SpO₂"))
                checkRadioGroup(rgSpO2, item.selected_option);
        }
    }

    private void restoreLocalSelections() {
        if (checkedAsa != -1) rgAsa.check(checkedAsa);
        if (checkedEx != -1) rgExercise.check(checkedEx);
        if (checkedDys != -1) rgDyspnea.check(checkedDys);
        if (checkedInf != -1) rgInfection.check(checkedInf);
        if (checkedSpO2 != -1) rgSpO2.check(checkedSpO2);
    }

    private void checkRadioGroup(RadioGroup group, String text) {
        if (text == null) return;

        String target = text.trim().toLowerCase();

        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton rb = (RadioButton) group.getChildAt(i);
            if (rb.getText().toString().trim().toLowerCase().equals(target)) {

                group.check(rb.getId());

                if (group == rgAsa) checkedAsa = rb.getId();
                if (group == rgExercise) checkedEx = rb.getId();
                if (group == rgDyspnea) checkedDys = rb.getId();
                if (group == rgInfection) checkedInf = rb.getId();
                if (group == rgSpO2) checkedSpO2 = rb.getId();
                break;
            }
        }
    }

    private void setListeners() {
        rgAsa.setOnCheckedChangeListener((g, i) -> checkedAsa = i);
        rgExercise.setOnCheckedChangeListener((g, i) -> checkedEx = i);
        rgDyspnea.setOnCheckedChangeListener((g, i) -> checkedDys = i);
        rgInfection.setOnCheckedChangeListener((g, i) -> checkedInf = i);
        rgSpO2.setOnCheckedChangeListener((g, i) -> checkedSpO2 = i);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MedicalHistoryActivity.class)
                    .putExtra("patient_id", patientId));
            finish();
        });
    }

    /** Reset static selections (used when a different patient opens the screen) */
    private static void clearStaticSelections() {
        checkedAsa = -1;
        checkedEx = -1;
        checkedDys = -1;
        checkedInf = -1;
        checkedSpO2 = -1;
    }

    /** -----------------------------------------------------------
     * SEND SURVEY
     * ----------------------------------------------------------- */
    private void sendSurvey() {

        List<Answer> answers = new ArrayList<>();
        int totalScore = 0;

        totalScore += addAsaScore(rgAsa, answers);
        totalScore += addExerciseScore(rgExercise, answers);
        totalScore += addYesNoScore(rgDyspnea, "Dyspnea at rest", 4, answers);
        totalScore += addYesNoScore(rgInfection, "Recent respiratory infection", 3, answers);
        totalScore += addSpO2Score(rgSpO2, answers);

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(totalScore);
        req.setStatus("preoperative_considerations");
        req.setRisk_level(getRiskLevel(totalScore));
        req.setSection_scores(Collections.singletonList(new SectionScore("Preoperative Considerations", totalScore)));
        req.setAnswers(answers);

        final int finalScore = totalScore;

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful()) {

                    Toast.makeText(
                            PreoperativeConsiderationsActivity.this,
                            "Saved! Score: " + finalScore,
                            Toast.LENGTH_SHORT
                    ).show();

                    startActivity(new Intent(
                            PreoperativeConsiderationsActivity.this,
                            SurgeryFactorsActivity.class
                    ).putExtra("patient_id", patientId));

                    finish();
                } else {
                    Toast.makeText(
                            PreoperativeConsiderationsActivity.this,
                            "Save failed: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(
                        PreoperativeConsiderationsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    /** -----------------------------------------------------------
     * SCORE HELPERS
     * ----------------------------------------------------------- */
    private int addAsaScore(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        RadioButton rb = findViewById(id);
        String v = rb.getText().toString();

        int score = 0;
        if (v.equals("I")) score = 0;
        else if (v.equals("II")) score = 2;
        else if (v.equals("III")) score = 4;
        else if (v.equals("IV")) score = 6;
        else if (v.equals("V")) score = 8;

        answers.add(new Answer("ASA Physical Status", v, score, "Preoperative Considerations"));
        return score;
    }

    private int addExerciseScore(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        RadioButton rb = findViewById(id);
        String v = rb.getText().toString();

        int score = (v.contains("<4") || v.toLowerCase().contains("less")) ? 3 : 0;

        answers.add(new Answer("Exercise tolerance", v, score, "Preoperative Considerations"));
        return score;
    }

    private int addYesNoScore(RadioGroup group, String key, int weight, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        RadioButton rb = findViewById(id);

        int score = rb.getText().toString().equalsIgnoreCase("Yes") ? weight : 0;

        answers.add(new Answer(key, rb.getText().toString(), score, "Preoperative Considerations"));
        return score;
    }

    private int addSpO2Score(RadioGroup group, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        RadioButton rb = findViewById(id);
        String v = rb.getText().toString();

        int score = 0;
        if (v.contains("96")) score = 0;
        else if (v.contains("91")) score = 2;
        else if (v.contains("90")) score = 4;

        answers.add(new Answer("SpO₂", v, score, "Preoperative Considerations"));
        return score;
    }

    private String getRiskLevel(int score) {
        if (score <= 2) return "Low";
        else if (score <= 5) return "Moderate";
        else return "High";
    }
}
