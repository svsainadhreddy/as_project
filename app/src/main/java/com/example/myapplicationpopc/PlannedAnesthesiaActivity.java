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

public class PlannedAnesthesiaActivity extends AppCompatActivity {

    private RadioGroup radioAriscat, radioVentilation, radioMuscle, radioReversal, radioAnalgesia;
    private Button btnNext;
    private ImageButton btnBack;

    private int patientScore = 0;
    private int medicalScore = 0;
    private int preopScore   = 0;
    private int surgeryScore = 0;
    private int patientId    = -1;

    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_anesthesia);

        // ---- Receive previous scores ----
        Intent fromPrev = getIntent();
        patientScore = fromPrev.getIntExtra("patient_score", 0);
        medicalScore = fromPrev.getIntExtra("medical_score", 0);
        preopScore   = fromPrev.getIntExtra("preop_score", 0);
        surgeryScore = fromPrev.getIntExtra("surgery_score", 0);
        patientId    = fromPrev.getIntExtra("patient_id", -1);

        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        token = "Token " + SharedPrefManager.getInstance(this).getToken();

        // ---- Bind Views ----
        radioAriscat     = findViewById(R.id.radioAriscat);
        radioVentilation = findViewById(R.id.radioVentilation);
        radioMuscle      = findViewById(R.id.radioMuscle);
        radioReversal    = findViewById(R.id.radioReversal);
        radioAnalgesia   = findViewById(R.id.radioAnalgesia);
        btnNext          = findViewById(R.id.btnNext);
        btnBack          = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, SurgeryFactorsActivity.class);
            startActivity(intent);
        });

        btnNext.setOnClickListener(v -> calculateAndSend());
    }

    private void calculateAndSend() {
        int tmpScore = 0;
        List<Answer> answers = new ArrayList<>();

        // --- ARISCAT Choice ---
        int idAriscat = radioAriscat.getCheckedRadioButtonId();
        if (idAriscat != -1) {
            RadioButton rb = findViewById(idAriscat);
            String choice = rb.getText().toString();
            int s = 0;
            // ✅ Classic switch for Java-8
            if ("Regional anesthesia (Spinal / Epidural / Nerve block)".equals(choice)) {
                s = 0;
            } else if ("General anesthesia with LMA".equals(choice)) {
                s = 2;
            } else if ("General anesthesia with ETT".equals(choice)) {
                s = 4;
            } else if ("Combined (GA + Regional)".equals(choice)) {
                s = 3;
            }
            tmpScore += s;
            answers.add(new Answer("ARISCAT Choice", choice, s));
        }

        // --- Ventilation strategy ---
        int idVent = radioVentilation.getCheckedRadioButtonId();
        if (idVent != -1) {
            RadioButton rb = findViewById(idVent);
            String choice = rb.getText().toString();
            int s = (choice.contains("Low tidal") || choice.contains("PEEP used")) ? 0 : 3;
            tmpScore += s;
            answers.add(new Answer("Ventilation Strategy", choice, s));
        }

        // --- Muscle relaxant use ---
        int idMuscle = radioMuscle.getCheckedRadioButtonId();
        if (idMuscle != -1) {
            RadioButton rb = findViewById(idMuscle);
            String choice = rb.getText().toString();
            int s = 0;
            if (!choice.equalsIgnoreCase("No")) {
                // add reversal
                int idRev = radioReversal.getCheckedRadioButtonId();
                if (idRev != -1) {
                    RadioButton rbRev = findViewById(idRev);
                    String rev = rbRev.getText().toString();
                    if (rev.contains("Neostigmine")) {
                        s = 2;
                    } else if (rev.contains("Sugammadex")) {
                        s = 1;
                    }
                    answers.add(new Answer("Reversal", rev, s));
                }
            }
            tmpScore += s;
            answers.add(new Answer("Muscle relaxant use", choice, (s == 0 ? 0 : s)));
        }

        // --- Planned analgesia ---
        int idAnal = radioAnalgesia.getCheckedRadioButtonId();
        if (idAnal != -1) {
            RadioButton rb = findViewById(idAnal);
            String choice = rb.getText().toString();
            int s = choice.contains("IV opioids") ? 3 : 0;
            tmpScore += s;
            answers.add(new Answer("Planned Analgesia", choice, s));
        }

        if (answers.isEmpty()) {
            Toast.makeText(this,
                    "Please answer at least one question",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final int plannedAnesthesiaScore = tmpScore;
        final int combinedTotal = patientScore + medicalScore + preopScore + surgeryScore + plannedAnesthesiaScore;

        // ---- Build request ----
        SurveyRequest request = new SurveyRequest();
        request.setPatient_id(patientId);
        request.setTotal_score(plannedAnesthesiaScore);
        List<SectionScore> sections = new ArrayList<>();
        sections.add(new SectionScore("Planned Anesthesia", plannedAnesthesiaScore));
        request.setSection_scores(sections);
        request.setAnswers(answers);

        // ---- POST to backend ----
        apiService.createSurvey(token, request).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PlannedAnesthesiaActivity.this,
                            "Planned Anesthesia saved. Score: " + plannedAnesthesiaScore,
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(PlannedAnesthesiaActivity.this,
                            PostoperativeActivity.class);
                    intent.putExtra("patient_id", patientId);
                    intent.putExtra("patient_score", patientScore);
                    intent.putExtra("medical_score", medicalScore);
                    intent.putExtra("preop_score", preopScore);
                    intent.putExtra("surgery_score", surgeryScore);
                    intent.putExtra("anesthetic_score", plannedAnesthesiaScore);
                    intent.putExtra("combined_total", combinedTotal);
                    intent.putExtra("survey_id", response.body().getId());
                    startActivity(intent);
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
}
