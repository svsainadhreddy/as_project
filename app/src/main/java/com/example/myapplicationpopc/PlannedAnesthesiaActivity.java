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

import org.json.JSONException;
import org.json.JSONObject;

public class PlannedAnesthesiaActivity extends AppCompatActivity {

    private RadioGroup radioAriscat, radioVentilation, radioMuscle, radioReversal, radioAnalgesia;
    private Button btnNext;
    private ImageButton btnBack;

    // 👉 Previous scores
    private int patientScore = 0;
    private int medicalScore = 0;
    private int preopScore   = 0;
    private int surgeryScore = 0;
    private int patientId = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_anesthesia);  // your XML filename

        // ✅ Receive scores from SurgeryFactorsActivity
        Intent fromPrev = getIntent();
        patientScore = fromPrev.getIntExtra("patient_score", 0);
        medicalScore = fromPrev.getIntExtra("medical_score", 0);
        preopScore   = fromPrev.getIntExtra("preop_score", 0);
        surgeryScore = fromPrev.getIntExtra("surgery_score", 0);
        // ✅ retrieve patient_id safely
        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
            // Optional: finish() if you don't want to allow proceeding
        }



        // --- Bind Views ---
        radioAriscat     = findViewById(R.id.radioAriscat);
        radioVentilation = findViewById(R.id.radioVentilation);
        radioMuscle      = findViewById(R.id.radioMuscle);
        radioReversal    = findViewById(R.id.radioReversal);
        radioAnalgesia   = findViewById(R.id.radioAnalgesia);
        btnNext          = findViewById(R.id.btnNext);
        btnBack          = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScores());
    }

    private void calculateScores() {
        int plannedAnesthesiaScore = 0;
        JSONObject answers = new JSONObject();

        // ===== ARISCAT Choice =====
        int idAriscat = radioAriscat.getCheckedRadioButtonId();
        if (idAriscat != -1) {
            RadioButton rb = findViewById(idAriscat);
            String choice = rb.getText().toString();
            try { answers.put("ARISCAT Choice", choice); } catch (JSONException ignored) {}

            switch (choice) {
                case "Regional anesthesia (Spinal / Epidural / Nerve block)":
                    plannedAnesthesiaScore += 0; break;
                case "General anesthesia with LMA":
                    plannedAnesthesiaScore += 2; break;
                case "General anesthesia with ETT":
                    plannedAnesthesiaScore += 4; break;
                case "Combined (GA + Regional)":
                    plannedAnesthesiaScore += 3; break;
            }
        }

        // ===== Ventilation strategy (if GA) =====
        int idVent = radioVentilation.getCheckedRadioButtonId();
        if (idVent != -1) {
            RadioButton rb = findViewById(idVent);
            String choice = rb.getText().toString();
            try { answers.put("Ventilation Strategy", choice); } catch (JSONException ignored) {}

            if (choice.contains("Low tidal") || choice.contains("PEEP used")) {
                plannedAnesthesiaScore += 0;
            } else if (choice.contains("High tidal") || choice.contains("Not used")) {
                plannedAnesthesiaScore += 3;
            }
        }

        // ===== Muscle relaxant use =====
        int idMuscle = radioMuscle.getCheckedRadioButtonId();
        if (idMuscle != -1) {
            RadioButton rb = findViewById(idMuscle);
            String choice = rb.getText().toString();
            try { answers.put("Muscle relaxant use", choice); } catch (JSONException ignored) {}

            if (choice.equalsIgnoreCase("No")) {
                plannedAnesthesiaScore += 0;
            } else {
                // If yes, add reversal choice separately
                int idRev = radioReversal.getCheckedRadioButtonId();
                if (idRev != -1) {
                    RadioButton rbRev = findViewById(idRev);
                    String rev = rbRev.getText().toString();
                    try { answers.put("Reversal", rev); } catch (JSONException ignored) {}

                    if (rev.contains("Neostigmine"))      plannedAnesthesiaScore += 2;
                    else if (rev.contains("Sugammadex"))  plannedAnesthesiaScore += 1;
                }
            }
        }

        // ===== Planned analgesia =====
        int idAnal = radioAnalgesia.getCheckedRadioButtonId();
        if (idAnal != -1) {
            RadioButton rb = findViewById(idAnal);
            String choice = rb.getText().toString();
            try { answers.put("Planned Analgesia", choice); } catch (JSONException ignored) {}

            if (choice.contains("IV opioids")) {
                plannedAnesthesiaScore += 3;
            }
        }

        Toast.makeText(this,
                "Planned Anesthesia Score: " + plannedAnesthesiaScore,
                Toast.LENGTH_LONG).show();

        // ✅ Pass all scores to PostoperativeActivity
        Intent intent = new Intent(this, PostoperativeActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("patient_score", patientScore);
        intent.putExtra("medical_score", medicalScore);
        intent.putExtra("preop_score", preopScore);
        intent.putExtra("surgery_score", surgeryScore);
        intent.putExtra("anesthetic_score", plannedAnesthesiaScore);
        intent.putExtra("planned_anesthesia_answers", answers.toString());
        startActivity(intent);
    }
}
