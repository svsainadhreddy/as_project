package com.example.myapplicationpopc;

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

import org.json.JSONException;
import org.json.JSONObject;

public class SurgeryFactorsActivity extends AppCompatActivity {

    private RadioGroup rgSurgeryType, rgUrgency, rgDuration, rgBloodLoss;
    private EditText etOtherSurgery;
    private Button btnNext;
    private ImageButton btnBack;

    // 👉 Scores from previous screens
    private int patientScore = 0;
    private int medicalScore = 0;
    private int preopScore   = 0;
    private int patientId = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgery_factors);

        // ✅ Receive previous scores
        Intent fromPrev = getIntent();
        patientScore = fromPrev.getIntExtra("patient_score", 0);
        medicalScore = fromPrev.getIntExtra("medical_score", 0);
        preopScore   = fromPrev.getIntExtra("preop_score", 0);
        // ✅ retrieve patient_id safely
        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
            // Optional: finish() if you don't want to allow proceeding
        }



        // --- Bind Views ---
        rgSurgeryType = findViewById(R.id.rgSurgeryType);
        rgUrgency     = findViewById(R.id.rgUrgency);
        rgDuration    = findViewById(R.id.rgDuration);
        rgBloodLoss   = findViewById(R.id.rgBloodLoss);
        etOtherSurgery= findViewById(R.id.etOtherSurgery);
        btnNext       = findViewById(R.id.btnNext);
        btnBack       = findViewById(R.id.btnBack);

        // Show textbox if "Others" is chosen
        rgSurgeryType.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb != null && rb.getText().toString().equalsIgnoreCase("Others")) {
                etOtherSurgery.setVisibility(View.VISIBLE);
            } else {
                etOtherSurgery.setVisibility(View.GONE);
            }
        });

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        int surgeryScore = 0;
        JSONObject answers = new JSONObject();

        // --- Type of surgery ---
        int idType = rgSurgeryType.getCheckedRadioButtonId();
        if (idType != -1) {
            RadioButton rb = findViewById(idType);
            String type = rb.getText().toString();
            if (type.equalsIgnoreCase("Others")) {
                String otherTxt = etOtherSurgery.getText().toString().trim();
                if (!otherTxt.isEmpty()) type = type + " (" + otherTxt + ")";
            }
            try { answers.put("Type of surgery", type); } catch (JSONException ignored) {}

            switch (rb.getText().toString()) {
                case "Thoracic":           surgeryScore += 7; break;
                case "Upper abdominal":    surgeryScore += 5; break;
                case "Lower abdominal":    surgeryScore += 3; break;
                case "Neurosurgery":       surgeryScore += 3; break;
                case "Orthopedic":         surgeryScore += 2; break;
                case "Ent / Head & neck":  surgeryScore += 2; break;
                case "Vascular / Cardiac": surgeryScore += 7; break;
                case "Others":             surgeryScore += 1; break;
            }
        }

        // --- Urgency ---
        int idUrg = rgUrgency.getCheckedRadioButtonId();
        if (idUrg != -1) {
            RadioButton rb = findViewById(idUrg);
            String urgency = rb.getText().toString();
            try { answers.put("Urgency", urgency); } catch (JSONException ignored) {}
            if (urgency.equalsIgnoreCase("Emergency")) surgeryScore += 4;
        }

        // --- Duration ---
        int idDur = rgDuration.getCheckedRadioButtonId();
        if (idDur != -1) {
            RadioButton rb = findViewById(idDur);
            String duration = rb.getText().toString();
            try { answers.put("Duration", duration); } catch (JSONException ignored) {}
            if (duration.contains("2–4") || duration.contains("2-4"))      surgeryScore += 3;
            else if (duration.contains(">4") || duration.contains("gt"))   surgeryScore += 5;
        }

        // --- Estimated Blood Loss ---
        int idLoss = rgBloodLoss.getCheckedRadioButtonId();
        if (idLoss != -1) {
            RadioButton rb = findViewById(idLoss);
            String loss = rb.getText().toString();
            try { answers.put("Estimated blood loss", loss); } catch (JSONException ignored) {}
            if (loss.contains("500–1000") || loss.contains("500-1000"))    surgeryScore += 2;
            else if (loss.contains(">1000") || loss.contains("gt"))        surgeryScore += 3;
        }

        Toast.makeText(this,
                "Surgery Factors Score: " + surgeryScore,
                Toast.LENGTH_SHORT).show();

        // ✅ Pass all accumulated scores to PlannedAnesthesiaActivity
        Intent intent = new Intent(this, PlannedAnesthesiaActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("patient_score", patientScore);
        intent.putExtra("medical_score", medicalScore);
        intent.putExtra("preop_score", preopScore);
        intent.putExtra("surgery_score", surgeryScore);
        intent.putExtra("surgery_answers", answers.toString());
        startActivity(intent);
    }
}
