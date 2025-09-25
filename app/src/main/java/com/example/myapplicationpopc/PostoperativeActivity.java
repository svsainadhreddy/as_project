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
import com.example.myapplicationpopc.model.SurveyResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;
import com.example.myapplicationpopc.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostoperativeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnNext;

    private RadioGroup groupIcu, groupAnalgesia, groupVentilation, groupMobilization;

    // Scores received from previous activities
    private int patientScore;
    private int medicalScore;
    private int preopScore;
    private int surgeryScore;
    private int plannedAnesthesiaScore;
    private int patientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postoperative);

        // --- Bind Views ---
        btnBack           = findViewById(R.id.btnBack);
        btnNext           = findViewById(R.id.btnNext);
        groupIcu          = findViewById(R.id.groupIcu);
        groupAnalgesia    = findViewById(R.id.groupAnalgesia);
        groupVentilation  = findViewById(R.id.groupVentilation);
        groupMobilization = findViewById(R.id.groupMobilization);

        // --- Retrieve all previous scores ---
        Intent in = getIntent();
        patientScore           = in.getIntExtra("patient_score", 0);
        medicalScore           = in.getIntExtra("medical_score", 0);
        preopScore             = in.getIntExtra("preop_score", 0);
        surgeryScore           = in.getIntExtra("surgery_score", 0);
        plannedAnesthesiaScore = in.getIntExtra("anesthetic_score", 0);
        patientId              = in.getIntExtra("patient_id", -1);

        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
        }

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        int postoperativeScore = 0;
        JSONObject answers = new JSONObject();

        // ICU/HDU admission
        int idIcu = groupIcu.getCheckedRadioButtonId();
        if (idIcu != -1) {
            RadioButton rb = findViewById(idIcu);
            String choice = rb.getText().toString();
            try { answers.put("Planned ICU/HDU admission", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.yes))) {
                postoperativeScore += 2;
            }
        }

        // Mechanical ventilation >24h
        int idVent = groupVentilation.getCheckedRadioButtonId();
        if (idVent != -1) {
            RadioButton rb = findViewById(idVent);
            String choice = rb.getText().toString();
            try { answers.put("Anticipated >24h ventilation", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.yes))) {
                postoperativeScore += 4;
            }
        }

        // Post-op analgesia
        int idAnal = groupAnalgesia.getCheckedRadioButtonId();
        if (idAnal != -1) {
            RadioButton rb = findViewById(idAnal);
            String choice = rb.getText().toString();
            try { answers.put("Post-op analgesia", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.opioid_heavy))) {
                postoperativeScore += 2;
            }
        }

        // Early mobilization within 24h
        int idMob = groupMobilization.getCheckedRadioButtonId();
        if (idMob != -1) {
            RadioButton rb = findViewById(idMob);
            String choice = rb.getText().toString();
            try { answers.put("Early mobilization within 24h", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.no))) {
                postoperativeScore += 2;
            }
        }

        // 👉 Calculate TOTAL score
        int totalScore = patientScore + medicalScore + preopScore +
                surgeryScore + plannedAnesthesiaScore + postoperativeScore;

        // ✅ Send only total score to backend
        sendTotalScoreToServer(patientId, totalScore);

        // 👉 Pass all scores to ScoreActivity for display only
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("patient_score",        patientScore);
        intent.putExtra("medical_score",        medicalScore);
        intent.putExtra("preop_score",          preopScore);
        intent.putExtra("surgery_score",        surgeryScore);
        intent.putExtra("anesthetic_score",     plannedAnesthesiaScore);
        intent.putExtra("postop_score",         postoperativeScore);
        intent.putExtra("total_score",          totalScore);
        startActivity(intent);
    }

    private void sendTotalScoreToServer(int patientId, int totalScore) {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "No token. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        SurveyRequest request = new SurveyRequest();
        request.setPatient_id(patientId);
        request.setTotal_score(totalScore);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.createSurvey("Bearer " + token, request).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PostoperativeActivity.this,
                            "✅ Total score saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PostoperativeActivity.this,
                            "⚠️ Failed to save (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(PostoperativeActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
