package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationpopc.model.SurveyResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScoreActivity extends AppCompatActivity {

    private ScoreBox patientDemo, medicalHistory, preop, surgery, anesthetic, postOp;
    private TextView tvTotalScore, tvManagement;
    private Button btnDone;

    // replace with your own session/token manager
    private SessionManager sessionManager;
    private int patientId;   // <- pass this from previous screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        sessionManager = new SessionManager(this);

        patientDemo    = new ScoreBox(findViewById(R.id.itemPatientDemo));
        medicalHistory = new ScoreBox(findViewById(R.id.itemMedicalHistory));
        preop          = new ScoreBox(findViewById(R.id.itemPreop));
        surgery        = new ScoreBox(findViewById(R.id.itemSurgery));
        anesthetic     = new ScoreBox(findViewById(R.id.itemAnesthetic));
        postOp         = new ScoreBox(findViewById(R.id.itemPostOp));

        tvTotalScore = findViewById(R.id.tvTotalScore);
        tvManagement = findViewById(R.id.tvManagement);
        btnDone      = findViewById(R.id.btnDone);

        // ✅ pull scores and patient id from previous activities
        Intent i = getIntent();
        int sPatient    = i.getIntExtra("patient_score", 0);
        int sMedical    = i.getIntExtra("medical_score", 0);
        int sPreop      = i.getIntExtra("preop_score", 0);
        int sSurgery    = i.getIntExtra("surgery_score", 0);
        int sAnesthetic = i.getIntExtra("anesthetic_score", 0);
        int sPostOp     = i.getIntExtra("postop_score", 0);
        patientId       = i.getIntExtra("patient_id", 0);

        // display each category
        patientDemo.setData("Patient Demographics", sPatient);
        medicalHistory.setData("Medical History", sMedical);
        preop.setData("Preoperative Functional Status", sPreop);
        surgery.setData("Surgery-Related Factors", sSurgery);
        anesthetic.setData("Anesthetic Factors", sAnesthetic);
        postOp.setData("Postoperative Considerations", sPostOp);

        // compute total
        int total = sPatient + sMedical + sPreop + sSurgery + sAnesthetic + sPostOp;
        tvTotalScore.setText(String.valueOf(total));
        tvManagement.setText(getManagementText(total));

        btnDone.setOnClickListener(view -> {
            postSurveyToServer(sPatient, sMedical, sPreop, sSurgery, sAnesthetic, sPostOp, total);
        });
    }

    /** Risk Stratification & management suggestion */
    private String getManagementText(int total) {
        if (total > 60) {
            return "Very high risk: Strongly consider avoiding GA/ETT if possible; "
                    + "optimize comorbidities pre-op, mandatory ICU planning.";
        } else if (total >= 41) {
            return "High risk: Prefer regional if feasible, strict lung-protective strategy, "
                    + "consider postoperative ICU/HDU.";
        } else if (total >= 21) {
            return "Moderate risk: Lung-protective ventilation, multimodal analgesia, "
                    + "encourage early mobilization.";
        } else {
            return "Low risk: Standard anesthesia; routine monitoring.";
        }
    }

    /** Send survey to Django */
    private void postSurveyToServer(int sPatient, int sMedical, int sPreop,
                                    int sSurgery, int sAnesthetic, int sPostOp, int total) {

        // build request
        SurveyResponse request = new SurveyResponse();
        request.setPatientId(patientId);
        request.setTotalScore(total);
        request.setRecommendation(getManagementText(total));

        // add each section score as key:value (section -> score)
        Map<String, Integer> sectionScores = new HashMap<>();
        sectionScores.put("Patient Demographics", sPatient);
        sectionScores.put("Medical History",      sMedical);
        sectionScores.put("Preoperative",         sPreop);
        sectionScores.put("Surgery Factors",      sSurgery);
        sectionScores.put("Anesthetic Factors",   sAnesthetic);
        sectionScores.put("Postoperative",        sPostOp);
        request.setSectionScores(sectionScores);

        // call API
        String token = "Token " + sessionManager.getAuthToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.createSurvey(token, request).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ScoreActivity.this, "Survey saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ScoreActivity.this, DoctorHomeActivity.class));
                } else {
                    Toast.makeText(ScoreActivity.this,
                            "Failed to save survey: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(ScoreActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Helper class to bind category name and score views */
    private static class ScoreBox {
        TextView name, score;
        ScoreBox(View root) {
            name  = root.findViewById(R.id.tvCategoryName);
            score = root.findViewById(R.id.tvCategoryScore);
        }
        void setData(String title, int value) {
            name.setText(title);
            score.setText(String.valueOf(value));
        }
    }
}
