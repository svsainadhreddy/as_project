package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class MedicalHistoryActivity extends AppCompatActivity {

    private RadioGroup rgCOPD, rgAsthma, rgOSA, rgILD, rgHeartFailure,
            rgCAD, rgHypertension, rgDiabetes, rgCKD;

    private Button btnNext;
    private ImageButton btnBack;

    private int patientId;
    private ApiService apiService;
    private String token;

    // Persisted selection ids (used to restore screen state). We will reset these when patient changes.
    private static int checkedCOPD = -1;
    private static int checkedAsthma = -1;
    private static int checkedOSA = -1;
    private static int checkedILD = -1;
    private static int checkedHeartFailure = -1;
    private static int checkedCAD = -1;
    private static int checkedHypertension = -1;
    private static int checkedDiabetes = -1;
    private static int checkedCKD = -1;

    // Track last patient id so we can clear previous selections for a new patient
    private static int lastPatientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);

        initViews();

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // If the patient changed since last time, clear the saved static selections
        if (lastPatientId != patientId) {
            clearStaticSelections(); // resets checked* to -1
            lastPatientId = patientId;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        String savedToken = SharedPrefManager.getInstance(this).getToken();
        if (savedToken == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        token = "Token " + savedToken;

        // Restore any *local* selections (only non -1 values will be applied)
        restoreLocalSelections();

        // Fetch backend selections; apply only if backend returns non-empty answers
        fetchSavedSelections();

        // listen to changes and persist into static fields (so navigation back/forth works for same patient)
        setLocalPersistenceListeners();

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this,
                    PatientDemographicsActivity.class).putExtra("patient_id", patientId));
            finish();
        });

        btnNext.setOnClickListener(v -> sendSurvey());
    }

    private void initViews() {
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        rgCOPD = findViewById(R.id.rgCOPD);
        rgAsthma = findViewById(R.id.rgAsthma);
        rgOSA = findViewById(R.id.rgOSA);
        rgILD = findViewById(R.id.rgILD);
        rgHeartFailure = findViewById(R.id.rgHeartFailure);
        rgCAD = findViewById(R.id.rgCAD);
        rgHypertension = findViewById(R.id.rgHypertension);
        rgDiabetes = findViewById(R.id.rgDiabetes);
        rgCKD = findViewById(R.id.rgCKD);
    }

    private void fetchSavedSelections() {
        apiService.getSectionAnswers(token, patientId, "Medical History")
                .enqueue(new Callback<SurveyAnswersResponse>() {
                    @Override
                    public void onResponse(Call<SurveyAnswersResponse> call,
                                           Response<SurveyAnswersResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<SurveyAnswersResponse.AnswerItem> answers = response.body().answers;
                            // only apply backend selections when list is non-null and non-empty
                            if (answers != null && !answers.isEmpty()) {
                                applyBackendSelections(answers);
                            }
                            // else: backend has no saved answers for this patient — leave UI as-is (no previous data shown)
                        }
                    }

                    @Override
                    public void onFailure(Call<SurveyAnswersResponse> call, Throwable t) {
                        Toast.makeText(MedicalHistoryActivity.this,
                                "Failed to fetch saved answers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyBackendSelections(List<SurveyAnswersResponse.AnswerItem> list) {
        if (list == null || list.isEmpty()) return;

        for (SurveyAnswersResponse.AnswerItem item : list) {
            switch (item.question) {
                case "COPD": checkGroupByText(rgCOPD, item.selected_option); break;
                case "Asthma": checkGroupByText(rgAsthma, item.selected_option); break;
                case "OSA": checkGroupByText(rgOSA, item.selected_option); break;
                case "ILD": checkGroupByText(rgILD, item.selected_option); break;
                case "Heart Failure": checkGroupByText(rgHeartFailure, item.selected_option); break;
                case "CAD": checkGroupByText(rgCAD, item.selected_option); break;
                case "Hypertension": checkGroupByText(rgHypertension, item.selected_option); break;
                case "Diabetes": checkGroupByText(rgDiabetes, item.selected_option); break;
                case "CKD": checkGroupByText(rgCKD, item.selected_option); break;
            }
        }
    }

    private void restoreLocalSelections() {
        if (checkedCOPD != -1) rgCOPD.check(checkedCOPD);
        if (checkedAsthma != -1) rgAsthma.check(checkedAsthma);
        if (checkedOSA != -1) rgOSA.check(checkedOSA);
        if (checkedILD != -1) rgILD.check(checkedILD);
        if (checkedHeartFailure != -1) rgHeartFailure.check(checkedHeartFailure);
        if (checkedCAD != -1) rgCAD.check(checkedCAD);
        if (checkedHypertension != -1) rgHypertension.check(checkedHypertension);
        if (checkedDiabetes != -1) rgDiabetes.check(checkedDiabetes);
        if (checkedCKD != -1) rgCKD.check(checkedCKD);
    }

    private void setLocalPersistenceListeners() {
        rgCOPD.setOnCheckedChangeListener((g, i) -> checkedCOPD = i);
        rgAsthma.setOnCheckedChangeListener((g, i) -> checkedAsthma = i);
        rgOSA.setOnCheckedChangeListener((g, i) -> checkedOSA = i);
        rgILD.setOnCheckedChangeListener((g, i) -> checkedILD = i);
        rgHeartFailure.setOnCheckedChangeListener((g, i) -> checkedHeartFailure = i);
        rgCAD.setOnCheckedChangeListener((g, i) -> checkedCAD = i);
        rgHypertension.setOnCheckedChangeListener((g, i) -> checkedHypertension = i);
        rgDiabetes.setOnCheckedChangeListener((g, i) -> checkedDiabetes = i);
        rgCKD.setOnCheckedChangeListener((g, i) -> checkedCKD = i);
    }

    private void checkGroupByText(RadioGroup group, String answer) {
        if (answer == null) return;
        String txt = answer.trim().toLowerCase();

        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton rb = (RadioButton) group.getChildAt(i);
            if (rb.getText().toString().trim().toLowerCase().equals(txt)) {
                group.check(rb.getId());
                return;
            }
        }
    }

    // Reset saved static selections
    private static void clearStaticSelections() {
        checkedCOPD = -1;
        checkedAsthma = -1;
        checkedOSA = -1;
        checkedILD = -1;
        checkedHeartFailure = -1;
        checkedCAD = -1;
        checkedHypertension = -1;
        checkedDiabetes = -1;
        checkedCKD = -1;
    }

    // -------------------------------------------------
    // VALIDATION: Ensure all fields selected
    // -------------------------------------------------
    private boolean validateAllFields() {
        if (rgCOPD.getCheckedRadioButtonId() == -1 ||
                rgAsthma.getCheckedRadioButtonId() == -1 ||
                rgOSA.getCheckedRadioButtonId() == -1 ||
                rgILD.getCheckedRadioButtonId() == -1 ||
                rgHeartFailure.getCheckedRadioButtonId() == -1 ||
                rgCAD.getCheckedRadioButtonId() == -1 ||
                rgHypertension.getCheckedRadioButtonId() == -1 ||
                rgDiabetes.getCheckedRadioButtonId() == -1 ||
                rgCKD.getCheckedRadioButtonId() == -1) {

            Toast.makeText(this,
                    "Please fill all fields before proceeding.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // -------------------------------------------------
    // SEND SURVEY
    // -------------------------------------------------
    private void sendSurvey() {

        // If some fields are empty: stop & show message
        if (!validateAllFields()) return;

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
        req.setTotal_score(sectionScore);
        req.setStatus("medical_history");
        req.setRisk_level(getRiskLevel(sectionScore));
        req.setSection_scores(Collections.singletonList(new SectionScore("Medical History", sectionScore)));
        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful()) {
                    startActivity(new Intent(MedicalHistoryActivity.this,
                            PreoperativeConsiderationsActivity.class)
                            .putExtra("patient_id", patientId));
                    finish();
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

    private int addDiseaseScore(RadioGroup group, String label, int weight, List<Answer> answers) {
        int id = group.getCheckedRadioButtonId();
        RadioButton btn = findViewById(id);
        String ans = btn.getText().toString();
        int score = ans.equalsIgnoreCase("Yes") ? weight : 0;

        answers.add(new Answer(label, ans, score, "Medical History"));
        return score;
    }

    private String getRiskLevel(int score) {
        if (score <= 5) return "Low";
        if (score <= 10) return "Moderate";
        return "High";
    }
}
