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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostoperativeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnNext;

    private RadioGroup groupIcu, groupAnalgesia, groupVentilation, groupMobilization;

    private int patientId = -1;
    private String token;

    private ApiService apiService;

    // ----------------------
    // RESTORE LOCAL UI STATE
    // ----------------------
    private static int checkedIcu = -1;
    private static int checkedAnalgesia = -1;
    private static int checkedVent = -1;
    private static int checkedMob = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postoperative);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);

        String savedToken = SharedPrefManager.getInstance(this).getToken();
        if (savedToken != null) {
            token = "Token " + savedToken;
        } else {
            Toast.makeText(this, "Login required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 🔄 Load backend answers
        fetchSavedSelections();

        // 🔄 Restore local UI
        restoreLocalSelections();

        // 🔄 Save selections locally
        groupIcu.setOnCheckedChangeListener((g, i) -> checkedIcu = i);
        groupAnalgesia.setOnCheckedChangeListener((g, i) -> checkedAnalgesia = i);
        groupVentilation.setOnCheckedChangeListener((g, i) -> checkedVent = i);
        groupMobilization.setOnCheckedChangeListener((g, i) -> checkedMob = i);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, PlannedAnesthesiaActivity.class)
                    .putExtra("patient_id", patientId));
            finish();
        });

        btnNext.setOnClickListener(v -> sendSurvey());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        groupIcu = findViewById(R.id.groupIcu);
        groupAnalgesia = findViewById(R.id.groupAnalgesia);
        groupVentilation = findViewById(R.id.groupVentilation);
        groupMobilization = findViewById(R.id.groupMobilization);
    }

    // ----------------------------------------------
    // FETCH SAVED ANSWERS FROM API AND APPLY TO UI
    // ----------------------------------------------
    private void fetchSavedSelections() {
        apiService.getSectionAnswers(token, patientId, "Postoperative")
                .enqueue(new Callback<SurveyAnswersResponse>() {
                    @Override
                    public void onResponse(Call<SurveyAnswersResponse> call, Response<SurveyAnswersResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            applySavedSelections(response.body().answers);
                        }
                    }

                    @Override
                    public void onFailure(Call<SurveyAnswersResponse> call, Throwable t) {
                        Toast.makeText(PostoperativeActivity.this,
                                "Failed to fetch saved answers.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applySavedSelections(List<SurveyAnswersResponse.AnswerItem> answers) {
        if (answers == null) return;

        for (SurveyAnswersResponse.AnswerItem item : answers) {
            switch (item.question) {
                case "Planned ICU/HDU admission":
                    checkRadio(groupIcu, item.selected_option);
                    break;
                case "Anticipated >24h ventilation":
                    checkRadio(groupVentilation, item.selected_option);
                    break;
                case "Post-op analgesia":
                    checkRadio(groupAnalgesia, item.selected_option);
                    break;
                case "Early mobilization within 24h":
                    checkRadio(groupMobilization, item.selected_option);
                    break;
            }
        }
    }

    // ----------------------------------------------
    // RESTORE LOCAL STATIC SELECTIONS
    // ----------------------------------------------
    private void restoreLocalSelections() {
        if (checkedIcu != -1) groupIcu.check(checkedIcu);
        if (checkedVent != -1) groupVentilation.check(checkedVent);
        if (checkedAnalgesia != -1) groupAnalgesia.check(checkedAnalgesia);
        if (checkedMob != -1) groupMobilization.check(checkedMob);
    }

    // Match text from backend to radio options
    private void checkRadio(RadioGroup group, String text) {
        if (text == null) return;
        String target = text.trim().toLowerCase();

        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton rb = (RadioButton) group.getChildAt(i);
            if (rb.getText().toString().trim().toLowerCase().equals(target)) {
                group.check(rb.getId());

                if (group == groupIcu) checkedIcu = rb.getId();
                else if (group == groupVentilation) checkedVent = rb.getId();
                else if (group == groupAnalgesia) checkedAnalgesia = rb.getId();
                else if (group == groupMobilization) checkedMob = rb.getId();

                break;
            }
        }
    }

    // ----------------------------------------------
    // SUBMIT SURVEY
    // ----------------------------------------------
    private void sendSurvey() {

        // Required fields
        if (groupIcu.getCheckedRadioButtonId() == -1 ||
                groupVentilation.getCheckedRadioButtonId() == -1 ||
                groupAnalgesia.getCheckedRadioButtonId() == -1 ||
                groupMobilization.getCheckedRadioButtonId() == -1) {

            Toast.makeText(this, "Please answer all questions.", Toast.LENGTH_LONG).show();
            return;
        }

        List<Answer> answers = new ArrayList<>();
        int sectionScore = 0;

        sectionScore += addIcu(answers);
        sectionScore += addVentilation(answers);
        sectionScore += addAnalgesia(answers);
        sectionScore += addMobilization(answers);

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(sectionScore);
        req.setStatus("postoperative");
        req.setRisk_level(getRiskLevel(sectionScore));

        List<SectionScore> sectionList = new ArrayList<>();
        sectionList.add(new SectionScore("Postoperative", sectionScore));
        req.setSection_scores(sectionList);

        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful()) {
                    startActivity(new Intent(PostoperativeActivity.this,
                            ScoreActivity.class).putExtra("patient_id", patientId));
                    finish();
                } else {
                    Toast.makeText(PostoperativeActivity.this,
                            "Save failed (" + response.code() + ")",
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

    private int addIcu(List<Answer> answers) {
        RadioButton rb = findViewById(groupIcu.getCheckedRadioButtonId());
        int score = rb.getText().toString().equalsIgnoreCase("Yes") ? 2 : 0;
        answers.add(new Answer("Planned ICU/HDU admission", rb.getText().toString(), score, "Postoperative"));
        return score;
    }

    private int addVentilation(List<Answer> answers) {
        RadioButton rb = findViewById(groupVentilation.getCheckedRadioButtonId());
        int score = rb.getText().toString().equalsIgnoreCase("Yes") ? 4 : 0;
        answers.add(new Answer("Anticipated >24h ventilation", rb.getText().toString(), score, "Postoperative"));
        return score;
    }

    private int addAnalgesia(List<Answer> answers) {
        RadioButton rb = findViewById(groupAnalgesia.getCheckedRadioButtonId());
        int score = rb.getText().toString().equalsIgnoreCase("Opioid heavy") ? 2 : 0;
        answers.add(new Answer("Post-op analgesia", rb.getText().toString(), score, "Postoperative"));
        return score;
    }

    private int addMobilization(List<Answer> answers) {
        RadioButton rb = findViewById(groupMobilization.getCheckedRadioButtonId());
        int score = rb.getText().toString().equalsIgnoreCase("No") ? 2 : 0;
        answers.add(new Answer("Early mobilization within 24h", rb.getText().toString(), score, "Postoperative"));
        return score;
    }

    private String getRiskLevel(int score) {
        if (score <= 5) return "Low";
        else if (score <= 10) return "Moderate";
        else if (score <= 15) return "High";
        return "Very High";
    }
}
