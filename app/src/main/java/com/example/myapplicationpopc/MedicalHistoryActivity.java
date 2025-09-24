package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MedicalHistoryActivity extends AppCompatActivity {

    private RadioGroup rgCOPD, rgAsthma, rgOSA, rgILD, rgHeartFailure,
            rgCAD, rgHypertension, rgDiabetes, rgCKD;
    private Button btnNext;
    private ImageButton btnBack;
    private int patientId = -1;


    // 👉 store the patient score received from previous screen
    private int patientScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);

        // --- receive score from PatientDemographicsActivity ---
        Intent fromPrev = getIntent();
        patientScore = fromPrev.getIntExtra("patient_score", 0);
        // ✅ retrieve patient_id safely
        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
            // Optional: finish() if you don't want to allow proceeding
        }


        // --- find views ---
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        rgCOPD        = findViewById(R.id.rgCOPD);
        rgAsthma      = findViewById(R.id.rgAsthma);
        rgOSA         = findViewById(R.id.rgOSA);
        rgILD         = findViewById(R.id.rgILD);
        rgHeartFailure= findViewById(R.id.rgHeartFailure);
        rgCAD         = findViewById(R.id.rgCAD);
        rgHypertension= findViewById(R.id.rgHypertension);
        rgDiabetes    = findViewById(R.id.rgDiabetes);
        rgCKD         = findViewById(R.id.rgCKD);

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        int medicalScore = 0;
        JSONObject answers = new JSONObject();

        // Helper function for each disease
        medicalScore += addDiseaseScore(rgCOPD,        "COPD", 3, answers);
        medicalScore += addDiseaseScore(rgAsthma,      "Asthma", 2, answers);
        medicalScore += addDiseaseScore(rgOSA,         "Obstructive Sleep Apnea", 2, answers);
        medicalScore += addDiseaseScore(rgILD,         "Interstitial Lung Disease", 3, answers);
        medicalScore += addDiseaseScore(rgHeartFailure,"Heart Failure", 2, answers);
        medicalScore += addDiseaseScore(rgCAD,         "CAD / Recent MI", 2, answers);
        medicalScore += addDiseaseScore(rgHypertension,"Hypertension", 1, answers);
        medicalScore += addDiseaseScore(rgDiabetes,    "Diabetes", 1, answers);
        medicalScore += addDiseaseScore(rgCKD,         "Chronic Kidney Disease", 2, answers);

        // Cap at 15
        if (medicalScore > 15) medicalScore = 15;

        Toast.makeText(this,
                "Medical History Score: " + medicalScore,
                Toast.LENGTH_SHORT).show();

        // 👉 Pass both patient_score and medical_score to next screen
        Intent intent = new Intent(this, PreoperativeConsiderationsActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("patient_score", patientScore);
        intent.putExtra("medical_score", medicalScore);
        intent.putExtra("medical_history_answers", answers.toString());
        startActivity(intent);
    }

    private int addDiseaseScore(RadioGroup group, String key, int weight, JSONObject answers) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == -1) return 0;

        RadioButton rb = findViewById(checkedId);
        String ans = rb.getText().toString();

        try { answers.put(key, ans); } catch (JSONException ignored) {}

        return ans.equalsIgnoreCase("Yes") ? weight : 0;
    }
}
