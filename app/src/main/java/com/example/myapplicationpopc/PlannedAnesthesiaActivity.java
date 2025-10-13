package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
    private View layoutReversal; // container for reversal question

    private int patientId = -1;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_anesthesia);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this, "⚠️ Invalid patient ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        token = "Token " + SharedPrefManager.getInstance(this).getToken();
        apiService = ApiClient.getClient().create(ApiService.class);

        radioAriscat     = findViewById(R.id.radioAriscat);
        radioVentilation = findViewById(R.id.radioVentilation);
        radioMuscle      = findViewById(R.id.radioMuscle);
        radioReversal    = findViewById(R.id.radioReversal);
        radioAnalgesia   = findViewById(R.id.radioAnalgesia);
        btnNext          = findViewById(R.id.btnNext);
        btnBack          = findViewById(R.id.btnBack);
        layoutReversal   = findViewById(R.id.layoutReversal);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, SurgeryFactorsActivity.class)
                    .putExtra("patient_id", patientId));
            finish();
        });

        // Show/hide reversal question based on Muscle Relaxants yes/no
        radioMuscle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMuscleYes) {
                layoutReversal.setVisibility(View.VISIBLE);
            } else {
                layoutReversal.setVisibility(View.GONE);
                radioReversal.clearCheck();
            }
        });

        btnNext.setOnClickListener(v -> sendSurvey());
    }

    private void sendSurvey() {
        List<Answer> answers = new ArrayList<>();
        int sectionScore = 0;

        // ARISCAT
        int idAriscat = radioAriscat.getCheckedRadioButtonId();
        if (idAriscat != -1) {
            RadioButton rb = findViewById(idAriscat);
            String choice = rb.getText().toString();
            int s = 0;
            if ("Regional anesthesia (Spinal / Epidural / Nerve block)".equals(choice)) s = 0;
            else if ("General anesthesia with LMA".equals(choice)) s = 2;
            else if ("General anesthesia with ETT".equals(choice)) s = 4;
            else if ("Combined (GA + Regional)".equals(choice)) s = 3;
            sectionScore += s;
            answers.add(new Answer("ARISCAT Choice", choice, s,"Planned Anesthesia"));
        }

        // Ventilation
        int idVent = radioVentilation.getCheckedRadioButtonId();
        if (idVent != -1) {
            RadioButton rb = findViewById(idVent);
            String choice = rb.getText().toString();
            int s = (choice.contains("Low tidal") || choice.contains("PEEP used")) ? 0 : 3;
            sectionScore += s;
            answers.add(new Answer("Ventilation Strategy", choice, s,"Planned Anesthesia"));
        }

        // Muscle relaxant + Reversal
        int idMuscle = radioMuscle.getCheckedRadioButtonId();
        if (idMuscle != -1) {
            RadioButton rb = findViewById(idMuscle);
            String choice = rb.getText().toString();
            int s = 0;
            if (!choice.equalsIgnoreCase("No")) {
                int idRev = radioReversal.getCheckedRadioButtonId();
                if (idRev != -1) {
                    RadioButton rbRev = findViewById(idRev);
                    String rev = rbRev.getText().toString();
                    if (rev.contains("Neostigmine")) s = 2;
                    else if (rev.contains("Sugammadex")) s = 1;
                    answers.add(new Answer("Reversal", rev, s,"Planned Anesthesia"));
                } else {
                    Toast.makeText(this, "Please select reversal method", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            sectionScore += s;
            answers.add(new Answer("Muscle relaxant use", choice, (s == 0 ? 0 : s),"Planned Anesthesia"));
        }

        // Planned Analgesia
        int idAnal = radioAnalgesia.getCheckedRadioButtonId();
        if (idAnal != -1) {
            RadioButton rb = findViewById(idAnal);
            String choice = rb.getText().toString();
            int s = choice.contains("IV opioids") ? 3 : 0;
            sectionScore += s;
            answers.add(new Answer("Planned Analgesia", choice, s,"Planned Anesthesia"));
        }

        if (answers.isEmpty()) {
            Toast.makeText(this, "Please answer at least one question", Toast.LENGTH_SHORT).show();
            return;
        }

        SurveyRequest req = new SurveyRequest();
        req.setPatient_id(patientId);
        req.setTotal_score(sectionScore);
        req.setStatus("planned_Anesthesia");
        req.setRisk_level(getRiskLevel(sectionScore));
        req.setSection_scores(List.of(new SectionScore("Planned Anesthesia", sectionScore)));
        req.setAnswers(answers);

        apiService.createSurvey(token, req).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PlannedAnesthesiaActivity.this, "Survey saved successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PlannedAnesthesiaActivity.this, PostoperativeActivity.class)
                            .putExtra("patient_id", patientId));
                    finish();
                } else {
                    Toast.makeText(PlannedAnesthesiaActivity.this, "Save failed (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(PlannedAnesthesiaActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getRiskLevel(int score) {
        if (score <= 2) return "Low";
        else if (score <= 5) return "Moderate";
        else return "High";
    }
}
