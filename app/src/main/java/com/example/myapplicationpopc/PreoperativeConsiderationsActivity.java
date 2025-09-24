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

public class PreoperativeConsiderationsActivity extends AppCompatActivity {

    private RadioGroup rgAsa, rgExercise, rgDyspnea, rgInfection, rgSpO2;
    private Button btnNext;
    private ImageButton btnBack;

    // 👉 scores received from previous activities
    private int patientScore = 0;
    private int medicalScore = 0;
    private int patientId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preoperative_functional_status);

        // --- receive previous scores ---
        Intent fromPrev = getIntent();
        patientScore = fromPrev.getIntExtra("patient_score", 0);
        medicalScore = fromPrev.getIntExtra("medical_score", 0);
        // ✅ retrieve patient_id safely
        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
            // Optional: finish() if you don't want to allow proceeding
        }


        // --- Bind Views ---
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        rgAsa = findViewById(R.id.rgAsa);
        rgExercise = findViewById(R.id.rgExercise);
        rgDyspnea = findViewById(R.id.rgDyspnea);
        rgInfection = findViewById(R.id.rgInfection);
        rgSpO2 = findViewById(R.id.rgSpO2);

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        int preopScore = 0;
        JSONObject answers = new JSONObject();

        // ASA Physical Status
        preopScore += addAsaScore(rgAsa, answers);

        // Exercise tolerance
        preopScore += addExerciseScore(rgExercise, answers);

        // Dyspnea at rest
        preopScore += addYesNoScore(rgDyspnea, "Dyspnea at rest", 4, answers);

        // Recent respiratory infection
        preopScore += addYesNoScore(rgInfection, "Recent respiratory infection", 3, answers);

        // SpO2 on room air
        preopScore += addSpO2Score(rgSpO2, answers);

        Toast.makeText(this,
                "Preoperative Functional Status Score: " + preopScore,
                Toast.LENGTH_SHORT).show();

        // 👉 Pass all accumulated scores to SurgeryFactorsActivity
        Intent intent = new Intent(this, SurgeryFactorsActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("patient_score", patientScore);
        intent.putExtra("medical_score", medicalScore);
        intent.putExtra("preop_score", preopScore);
        intent.putExtra("preop_answers", answers.toString());
        startActivity(intent);
    }

    private int addAsaScore(RadioGroup group, JSONObject answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String value = rb.getText().toString();
        try { answers.put("ASA Physical Status", value); } catch (JSONException ignored) {}

        switch (value) {
            case "I":  return 0;
            case "II": return 2;
            case "III":return 4;
            case "IV": return 6;
            case "V":  return 8;
        }
        return 0;
    }

    private int addExerciseScore(RadioGroup group, JSONObject answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String value = rb.getText().toString();
        try { answers.put("Exercise tolerance", value); } catch (JSONException ignored) {}

        // ≥4 METs = 0 ; <4 METs = 3
        if (value.contains("<4") || value.contains("less")) return 3;
        return 0;
    }

    private int addYesNoScore(RadioGroup group, String key, int weight, JSONObject answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String value = rb.getText().toString();
        try { answers.put(key, value); } catch (JSONException ignored) {}
        return value.equalsIgnoreCase("Yes") ? weight : 0;
    }

    private int addSpO2Score(RadioGroup group, JSONObject answers) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return 0;
        RadioButton rb = findViewById(id);
        String value = rb.getText().toString();
        try { answers.put("SpO₂", value); } catch (JSONException ignored) {}

        // ≥96% = 0, 91–95% = 2, ≤90% = 4
        if (value.contains("96")) return 0;
        if (value.contains("91")) return 2;
        if (value.contains("90")) return 4;
        return 0;
    }
}
