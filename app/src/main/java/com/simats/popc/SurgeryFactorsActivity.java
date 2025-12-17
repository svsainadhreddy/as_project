package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurgeryFactorsActivity extends AppCompatActivity {

    private RadioGroup rgSurgeryType, rgUrgency, rgDuration, rgBloodLoss;
    private EditText etOtherSurgery;
    private Button btnNext;
    private ImageButton btnBack;

    private int patientId;
    private String token;
    private ApiService apiService;

    /** Persist UI selections **/
    private static int checkedType = -1;
    private static int checkedUrgency = -1;
    private static int checkedDuration = -1;
    private static int checkedBloodLoss = -1;
    private static String savedOtherText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgery_factors);

        initViews();
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String savedToken = SharedPrefManager.getInstance(this).getToken();
        if (savedToken == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        token = "Token " + savedToken;
        apiService = ApiClient.getClient().create(ApiService.class);

        fetchSavedSelections();   // Restore from backend
        restoreLocalSelections(); // Restore from local static vars

        // Updating static saved selection
        rgSurgeryType.setOnCheckedChangeListener((g, i) -> {
            checkedType = i;
            updateOtherVisibility();
        });

        rgUrgency.setOnCheckedChangeListener((g, i) -> checkedUrgency = i);
        rgDuration.setOnCheckedChangeListener((g, i) -> checkedDuration = i);
        rgBloodLoss.setOnCheckedChangeListener((g, i) -> checkedBloodLoss = i);

        btnBack.setOnClickListener(v -> {
            savedOtherText = etOtherSurgery.getText().toString();
            startActivity(new Intent(this, PreoperativeConsiderationsActivity.class)
                    .putExtra("patient_id", patientId));
            finish();
        });

        btnNext.setOnClickListener(v -> {
            savedOtherText = etOtherSurgery.getText().toString();
            sendSurvey();
        });
    }

    private void initViews() {
        rgSurgeryType = findViewById(R.id.rgSurgeryType);
        rgUrgency = findViewById(R.id.rgUrgency);
        rgDuration = findViewById(R.id.rgDuration);
        rgBloodLoss = findViewById(R.id.rgBloodLoss);
        etOtherSurgery = findViewById(R.id.etOtherSurgery);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
    }

    // ============================================================
    // FETCH SAVED ANSWERS FROM API
    // ============================================================
    private void fetchSavedSelections() {

        apiService.getSectionAnswers(token, patientId, "Surgery Factors")
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
                        Toast.makeText(SurgeryFactorsActivity.this,
                                "Failed to restore saved answers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applySavedSelections(List<SurveyAnswersResponse.AnswerItem> list) {

        if (list == null) return;

        for (SurveyAnswersResponse.AnswerItem item : list) {

            switch (item.question) {

                case "Type of surgery":
                    checkRadioGroup(rgSurgeryType, item.selected_option);
                    if (item.selected_option.toLowerCase().contains("others(")) {
                        savedOtherText = extractOtherText(item.selected_option);
                    }
                    break;

                case "Urgency":
                    checkRadioGroup(rgUrgency, item.selected_option);
                    break;

                case "Duration":
                    checkRadioGroup(rgDuration, item.selected_option);
                    break;

                case "Estimated blood loss":
                    checkRadioGroup(rgBloodLoss, item.selected_option);
                    break;
            }
        }

        etOtherSurgery.setText(savedOtherText);
        updateOtherVisibility();
    }

    private String extractOtherText(String val) {
        if (!val.contains("(")) return "";
        return val.substring(val.indexOf("(") + 1, val.indexOf(")")).trim();
    }

    // ============================================================
    // RESTORE LOCAL UI STATE
    // ============================================================
    private void restoreLocalSelections() {

        if (checkedType != -1) rgSurgeryType.check(checkedType);
        if (checkedUrgency != -1) rgUrgency.check(checkedUrgency);
        if (checkedDuration != -1) rgDuration.check(checkedDuration);
        if (checkedBloodLoss != -1) rgBloodLoss.check(checkedBloodLoss);

        etOtherSurgery.setText(savedOtherText);
        updateOtherVisibility();
    }

    private void checkRadioGroup(RadioGroup group, String text) {
        if (text == null) return;

        text = text.trim().toLowerCase();

        for (int i = 0; i < group.getChildCount(); i++) {
            View v = group.getChildAt(i);

            if (v instanceof RadioButton) {

                RadioButton rb = (RadioButton) v;
                String rbText = rb.getText().toString().trim().toLowerCase();

                if (rbText.equals(text) || text.startsWith(rbText)) {

                    group.check(rb.getId());

                    if (group == rgSurgeryType) checkedType = rb.getId();
                    if (group == rgUrgency) checkedUrgency = rb.getId();
                    if (group == rgDuration) checkedDuration = rb.getId();
                    if (group == rgBloodLoss) checkedBloodLoss = rb.getId();

                    return;
                }
            }
        }
    }

    private void updateOtherVisibility() {

        if (checkedType == -1) {
            etOtherSurgery.setVisibility(View.GONE);
            return;
        }

        RadioButton rb = findViewById(checkedType);

        if (rb != null && rb.getText().toString().equalsIgnoreCase("Others")) {
            etOtherSurgery.setVisibility(View.VISIBLE);
        } else {
            etOtherSurgery.setVisibility(View.GONE);
        }
    }

    // ============================================================
    // SEND SURVEY
    // ============================================================
    private void sendSurvey() {

        // VALIDATION - Required for all fields
        if (checkedType == -1 || checkedUrgency == -1 ||
                checkedDuration == -1 || checkedBloodLoss == -1) {

            Toast.makeText(this,
                    "Please answer all questions!", Toast.LENGTH_LONG).show();
            return;
        }

        RadioButton typeRB = findViewById(checkedType);
        RadioButton urgRB = findViewById(checkedUrgency);
        RadioButton durRB = findViewById(checkedDuration);
        RadioButton lossRB = findViewById(checkedBloodLoss);

        String type = typeRB.getText().toString();
        String urgency = urgRB.getText().toString();
        String duration = durRB.getText().toString();
        String bloodLoss = lossRB.getText().toString();

        // Validate Others text
        if (typeRB.getText().toString().equalsIgnoreCase("Others")) {
            if (savedOtherText.trim().isEmpty()) {
                Toast.makeText(this,
                        "Please specify surgery under 'Others'", Toast.LENGTH_LONG).show();
                return;
            }
            type = "Others (" + savedOtherText + ")";
        }

        // SCORE COMPILE
        int scoreType = getTypeScore(typeRB.getText().toString());
        int scoreUrg = urgency.equalsIgnoreCase("Emergency") ? 4 : 0;
        int scoreDur = getDurationScore(duration);
        int scoreLoss = getBloodLossScore(bloodLoss);

        int totalScore = scoreType + scoreUrg + scoreDur + scoreLoss;

        List<Answer> answers = new ArrayList<>();
        answers.add(new Answer("Type of surgery", type, scoreType, "Surgery Factors"));
        answers.add(new Answer("Urgency", urgency, scoreUrg, "Surgery Factors"));
        answers.add(new Answer("Duration", duration, scoreDur, "Surgery Factors"));
        answers.add(new Answer("Estimated blood loss", bloodLoss, scoreLoss, "Surgery Factors"));

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(totalScore);
        req.setStatus("surgery_Factors");
        req.setRisk_level(getRisk(totalScore));
        req.setSection_scores(Collections.singletonList(
                new SectionScore("Surgery Factors", totalScore)
        ));
        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call,
                                   Response<SurveyResponse> response) {

                if (response.isSuccessful()) {
                    startActivity(new Intent(SurgeryFactorsActivity.this,
                            PlannedAnesthesiaActivity.class)
                            .putExtra("patient_id", patientId));
                    finish();
                } else {
                    Toast.makeText(SurgeryFactorsActivity.this,
                            "Save failed: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(SurgeryFactorsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private int getTypeScore(String type) {
        type = type.toLowerCase();
        if (type.equals("thoracic")) return 7;
        if (type.equals("upper abdominal")) return 5;
        if (type.equals("lower abdominal")) return 3;
        if (type.equals("neurosurgery")) return 3;
        if (type.equals("orthopedic")) return 2;
        if (type.equals("ent / head & neck")) return 2;
        if (type.equals("vascular / cardiac")) return 7;
        if (type.equals("others")) return 1;
        return 0;
    }

    private int getDurationScore(String dur) {
        dur = dur.toLowerCase();
        if (dur.contains("2") && dur.contains("4")) return 3;
        if (dur.contains(">4")) return 5;
        return 0;
    }

    private int getBloodLossScore(String loss) {
        loss = loss.toLowerCase();
        if (loss.contains("500") && loss.contains("1000")) return 2;
        if (loss.contains(">1000")) return 3;
        return 0;
    }

    private String getRisk(int score) {
        if (score <= 2) return "Low";
        if (score <= 5) return "Moderate";
        return "High";
    }
}
