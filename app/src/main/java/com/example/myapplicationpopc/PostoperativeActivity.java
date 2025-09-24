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

public class PostoperativeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnNext;

    private RadioGroup groupIcu, groupAnalgesia, groupVentilation, groupMobilization;

    // ✅ values passed from PlannedAnesthesiaActivity
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

        btnBack          = findViewById(R.id.btnBack);
        btnNext          = findViewById(R.id.btnNext);
        groupIcu         = findViewById(R.id.groupIcu);
        groupAnalgesia   = findViewById(R.id.groupAnalgesia);
        groupVentilation = findViewById(R.id.groupVentilation);
        groupMobilization= findViewById(R.id.groupMobilization);

        // ✅ retrieve all previous scores
        Intent in = getIntent();
        patientScore         = in.getIntExtra("patient_score", 0);
        medicalScore         = in.getIntExtra("medical_score", 0);
        preopScore           = in.getIntExtra("preop_score", 0);
        surgeryScore         = in.getIntExtra("surgery_score", 0);
        plannedAnesthesiaScore = in.getIntExtra("anesthetic_score", 0);
        // ✅ retrieve patient_id safely
        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
            // Optional: finish() if you don't want to allow proceeding
        }



        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        int postoperativeScore = 0;
        JSONObject answers = new JSONObject();

        // --- ICU/HDU admission ---
        int idIcu = groupIcu.getCheckedRadioButtonId();
        if (idIcu != -1) {
            RadioButton rb = findViewById(idIcu);
            String choice = rb.getText().toString();
            try { answers.put("Planned ICU/HDU admission", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.yes))) {
                postoperativeScore += 2;
            }
        }

        // --- Mechanical ventilation >24h ---
        int idVent = groupVentilation.getCheckedRadioButtonId();
        if (idVent != -1) {
            RadioButton rb = findViewById(idVent);
            String choice = rb.getText().toString();
            try { answers.put("Anticipated >24h ventilation", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.yes))) {
                postoperativeScore += 4;
            }
        }

        // --- Post-op analgesia ---
        int idAnal = groupAnalgesia.getCheckedRadioButtonId();
        if (idAnal != -1) {
            RadioButton rb = findViewById(idAnal);
            String choice = rb.getText().toString();
            try { answers.put("Post-op analgesia", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.opioid_heavy))) {
                postoperativeScore += 2;
            }
        }

        // --- Early mobilization within 24h ---
        int idMob = groupMobilization.getCheckedRadioButtonId();
        if (idMob != -1) {
            RadioButton rb = findViewById(idMob);
            String choice = rb.getText().toString();
            try { answers.put("Early mobilization within 24h", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.no))) {
                postoperativeScore += 2;
            }
        }

        Toast.makeText(this,
                "Postoperative Score: " + postoperativeScore,
                Toast.LENGTH_LONG).show();

        // ✅ send ALL category scores to ScoreActivity
        Intent intent = new Intent(PostoperativeActivity.this, ScoreActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("patient_score",            patientScore);
        intent.putExtra("medical_score",            medicalScore);
        intent.putExtra("preop_score",              preopScore);
        intent.putExtra("surgery_score",            surgeryScore);
        intent.putExtra("anesthetic_score",plannedAnesthesiaScore);
        intent.putExtra("postop_score",             postoperativeScore);
        intent.putExtra("postoperative_answers",    answers.toString());

        startActivity(intent);
    }
}
