package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class PlannedAnesthesiaActivity extends AppCompatActivity {

    private RadioGroup rgAriscat, rgVentilation, rgMuscle, rgReversal, rgAnalgesia;
    private Button btnNext;
    private ImageButton btnBack;
    private View layoutReversal;

    private int patientId = -1;
    private String token;
    private ApiService apiService;

    // ---------- STATIC STORE (persist on screen navigation) ----------
    private static int checkedAriscat = -1;
    private static int checkedVent = -1;
    private static int checkedMuscle = -1;
    private static int checkedReversal = -1;
    private static int checkedAnalgesia = -1;

    // NEW: track last patient so we clear statics when patient changes
    private static int lastPatientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_anesthesia);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // If switched patient, clear persisted static selections
        if (lastPatientId != patientId) {
            clearStaticSelections();
            lastPatientId = patientId;
        }

        String savedToken = SharedPrefManager.getInstance(this).getToken();
        if (savedToken == null || savedToken.trim().isEmpty()) {
            Toast.makeText(this, "Login required", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        token = "Token " + savedToken.trim();

        apiService = ApiClient.getClient().create(ApiService.class);

        fetchSavedSelections();
        restoreLocalSelections();

        // UI listeners
        rgMuscle.setOnCheckedChangeListener((g, id) -> {
            checkedMuscle = id;

            if (id == R.id.rbMuscleYes) {
                layoutReversal.setVisibility(View.VISIBLE);
            } else {
                layoutReversal.setVisibility(View.GONE);
                rgReversal.clearCheck();
                checkedReversal = -1;
            }
        });

        rgAriscat.setOnCheckedChangeListener((g, id) -> checkedAriscat = id);
        rgVentilation.setOnCheckedChangeListener((g, id) -> checkedVent = id);
        rgReversal.setOnCheckedChangeListener((g, id) -> checkedReversal = id);
        rgAnalgesia.setOnCheckedChangeListener((g, id) -> checkedAnalgesia = id);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, SurgeryFactorsActivity.class)
                    .putExtra("patient_id", patientId));
            finish();
        });

        btnNext.setOnClickListener(v -> sendSurvey());
    }

    private void initViews() {
        rgAriscat = findViewById(R.id.radioAriscat);
        rgVentilation = findViewById(R.id.radioVentilation);
        rgMuscle = findViewById(R.id.radioMuscle);
        rgReversal = findViewById(R.id.radioReversal);
        rgAnalgesia = findViewById(R.id.radioAnalgesia);
        layoutReversal = findViewById(R.id.layoutReversal);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
    }

    // -------------------- FETCH SAVED UI STATE FROM BACKEND --------------------
    private void fetchSavedSelections() {
        apiService.getSectionAnswers(token, patientId, "Planned Anesthesia")
                .enqueue(new Callback<SurveyAnswersResponse>() {
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
                        Toast.makeText(PlannedAnesthesiaActivity.this,
                                "Failed to load saved answers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ----------------------- APPLY BACKEND RESTORE ---------------------------
    private void applySavedSelections(List<SurveyAnswersResponse.AnswerItem> list) {
        if (list == null || list.isEmpty()) return;

        for (SurveyAnswersResponse.AnswerItem item : list) {

            switch (item.question) {
                case "ARISCAT Choice":
                    checkRadioGroup(rgAriscat, item.selected_option);
                    break;

                case "Ventilation Strategy":
                    checkRadioGroup(rgVentilation, item.selected_option);
                    break;

                case "Muscle relaxant use":
                    checkRadioGroup(rgMuscle, item.selected_option);

                    if (item.selected_option != null && item.selected_option.toLowerCase().contains("yes"))
                        layoutReversal.setVisibility(View.VISIBLE);
                    break;

                case "Reversal":
                    checkRadioGroup(rgReversal, item.selected_option);
                    break;

                case "Planned Analgesia":
                    checkRadioGroup(rgAnalgesia, item.selected_option);
                    break;
            }
        }
    }

    // ------------------------- RESTORE STATIC DATA -------------------------
    private void restoreLocalSelections() {

        if (checkedAriscat != -1) rgAriscat.check(checkedAriscat);
        if (checkedVent != -1) rgVentilation.check(checkedVent);
        if (checkedMuscle != -1) rgMuscle.check(checkedMuscle);
        if (checkedReversal != -1) rgReversal.check(checkedReversal);
        if (checkedAnalgesia != -1) rgAnalgesia.check(checkedAnalgesia);

        if (checkedMuscle == R.id.rbMuscleYes)
            layoutReversal.setVisibility(View.VISIBLE);
        else
            layoutReversal.setVisibility(View.GONE);
    }

    private void checkRadioGroup(RadioGroup rg, String text) {
        if (text == null) return;
        String t = text.toLowerCase();

        for (int i = 0; i < rg.getChildCount(); i++) {
            View v = rg.getChildAt(i);
            if (v instanceof RadioButton) {
                RadioButton rb = (RadioButton) v;

                if (rb.getText().toString().toLowerCase().equals(t)) {

                    rg.check(rb.getId());

                    if (rg == rgAriscat) checkedAriscat = rb.getId();
                    if (rg == rgVentilation) checkedVent = rb.getId();
                    if (rg == rgMuscle) checkedMuscle = rb.getId();
                    if (rg == rgReversal) checkedReversal = rb.getId();
                    if (rg == rgAnalgesia) checkedAnalgesia = rb.getId();

                    break;
                }
            }
        }
    }

    // ---------------------------- VALIDATION + SUBMIT ----------------------------
    private void sendSurvey() {

        // -------- All questions MUST be filled --------
        if (rgAriscat.getCheckedRadioButtonId() == -1 ||
                rgVentilation.getCheckedRadioButtonId() == -1 ||
                rgMuscle.getCheckedRadioButtonId() == -1 ||
                rgAnalgesia.getCheckedRadioButtonId() == -1) {

            Toast.makeText(this,
                    "Please fill all fields before proceeding!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // -------- Reversal is *required* only if Muscle = Yes --------
        if (rgMuscle.getCheckedRadioButtonId() == R.id.rbMuscleYes &&
                rgReversal.getCheckedRadioButtonId() == -1) {

            Toast.makeText(this,
                    "Please select a reversal method!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // ------------------- SCORING -------------------
        List<Answer> answers = new ArrayList<>();
        int total = 0;

        total += scoreAriscat(answers);
        total += scoreVentilation(answers);
        total += scoreMuscleRelaxant(answers);
        total += scoreAnalgesia(answers);

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(total);
        req.setStatus("planned_Anesthesia");
        req.setRisk_level(getRisk(total));
        req.setSection_scores(Collections.singletonList(new SectionScore("Planned Anesthesia", total)));
        req.setAnswers(answers);

        apiService.createSurvey(token, req)
                .enqueue(new Callback<SurveyResponse>() {
                    @Override
                    public void onResponse(Call<SurveyResponse> call,
                                           Response<SurveyResponse> response) {

                        if (response.isSuccessful()) {

                            startActivity(new Intent(
                                    PlannedAnesthesiaActivity.this,
                                    PostoperativeActivity.class
                            ).putExtra("patient_id", patientId));

                            finish();
                        } else {
                            Toast.makeText(PlannedAnesthesiaActivity.this,
                                    "Save failed: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SurveyResponse> call, Throwable t) {
                        Toast.makeText(PlannedAnesthesiaActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ----------------------------- SCORING FUNCTIONS -----------------------------
    private int scoreAriscat(List<Answer> answers) {
        RadioButton rb = findViewById(rgAriscat.getCheckedRadioButtonId());
        String c = rb.getText().toString();

        int s = 0;
        if (c.contains("Regional")) s = 0;
        else if (c.contains("LMA")) s = 2;
        else if (c.contains("ETT")) s = 4;
        else if (c.contains("Combined")) s = 3;

        answers.add(new Answer("ARISCAT Choice", c, s, "Planned Anesthesia"));
        return s;
    }

    private int scoreVentilation(List<Answer> answers) {
        RadioButton rb = findViewById(rgVentilation.getCheckedRadioButtonId());
        String c = rb.getText().toString();

        int s = (c.contains("Low tidal") || c.contains("PEEP")) ? 0 : 3;

        answers.add(new Answer("Ventilation Strategy", c, s, "Planned Anesthesia"));
        return s;
    }

    private int scoreMuscleRelaxant(List<Answer> answers) {
        RadioButton rb = findViewById(rgMuscle.getCheckedRadioButtonId());
        String c = rb.getText().toString();

        int s = 0;

        if (c.contains("Yes")) {
            RadioButton rbRev = findViewById(rgReversal.getCheckedRadioButtonId());
            String rev = rbRev.getText().toString();

            if (rev.contains("Neostigmine")) s = 2;
            else if (rev.contains("Sugammadex")) s = 1;

            answers.add(new Answer("Reversal", rev, s, "Planned Anesthesia"));
        }

        answers.add(new Answer("Muscle relaxant use", c, s, "Planned Anesthesia"));
        return s;
    }

    private int scoreAnalgesia(List<Answer> answers) {
        RadioButton rb = findViewById(rgAnalgesia.getCheckedRadioButtonId());
        String c = rb.getText().toString();

        int s = c.contains("IV opioids") ? 3 : 0;

        answers.add(new Answer("Planned Analgesia", c, s, "Planned Anesthesia"));
        return s;
    }

    private String getRisk(int score) {
        if (score <= 2) return "Low";
        if (score <= 5) return "Moderate";
        return "High";
    }

    // Reset static selections (used when a different patient opens the screen)
    private static void clearStaticSelections() {
        checkedAriscat = -1;
        checkedVent = -1;
        checkedMuscle = -1;
        checkedReversal = -1;
        checkedAnalgesia = -1;
    }
}
