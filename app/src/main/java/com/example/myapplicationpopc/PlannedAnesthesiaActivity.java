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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_anesthesia);  // your XML filename

        radioAriscat   = findViewById(R.id.radioAriscat);
        radioVentilation = findViewById(R.id.radioVentilation);
        radioMuscle    = findViewById(R.id.radioMuscle);
        radioReversal  = findViewById(R.id.radioReversal);
        radioAnalgesia = findViewById(R.id.radioAnalgesia);
        btnNext        = findViewById(R.id.btnNext);
        btnBack        = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScores());
    }

    private void calculateScores() {
        int totalScore = 0;
        JSONObject answers = new JSONObject();

        // ===== ARISCAT Score =====
        int idAriscat = radioAriscat.getCheckedRadioButtonId();
        if (idAriscat != -1) {
            RadioButton rb = findViewById(idAriscat);
            String choice = rb.getText().toString();
            try { answers.put("ARISCAT Choice", choice); } catch (JSONException ignored) {}

            switch (choice) {
                case "Regional anesthesia (Spinal / Epidural / Nerve block)":
                    totalScore += 0; break;
                case "General anesthesia with LMA":
                    totalScore += 2; break;
                case "General anesthesia with ETT":
                    totalScore += 4; break;
                case "Combined (GA + Regional)":
                    totalScore += 3; break;
            }
        }

        // ===== Ventilation strategy (if GA) =====
        int idVent = radioVentilation.getCheckedRadioButtonId();
        if (idVent != -1) {
            RadioButton rb = findViewById(idVent);
            String choice = rb.getText().toString();
            try { answers.put("Ventilation Strategy", choice); } catch (JSONException ignored) {}

            // Low tidal + PEEP = 0, High tidal or no PEEP = 3
            if (choice.contains("Low tidal") || choice.contains("PEEP used")) {
                totalScore += 0;
            } else if (choice.contains("High tidal") || choice.contains("Not used")) {
                totalScore += 3;
            }
        }

        // ===== Muscle relaxant use =====
        int idMuscle = radioMuscle.getCheckedRadioButtonId();
        if (idMuscle != -1) {
            RadioButton rb = findViewById(idMuscle);
            String choice = rb.getText().toString();
            try { answers.put("Muscle relaxant use", choice); } catch (JSONException ignored) {}

            if (choice.equalsIgnoreCase("No")) {
                totalScore += 0;
            } else {
                // If yes, add reversal choice separately
                int idRev = radioReversal.getCheckedRadioButtonId();
                if (idRev != -1) {
                    RadioButton rbRev = findViewById(idRev);
                    String rev = rbRev.getText().toString();
                    try { answers.put("Reversal", rev); } catch (JSONException ignored) {}

                    if (rev.contains("Neostigmine"))      totalScore += 2;
                    else if (rev.contains("Sugammadex"))  totalScore += 1;
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
                totalScore += 3;
            } else {
                // Regional / multimodal non-opioid = 0
                totalScore += 0;
            }
        }

        Toast.makeText(this, "Planned Anesthesia Score: " + totalScore, Toast.LENGTH_LONG).show();

        // === Pass to next activity if needed ===
        Intent intent = new Intent(this,PostoperativeActivity.class); // replace with your next screen
        intent.putExtra("planned_anesthesia_score", totalScore);
        intent.putExtra("planned_anesthesia_answers", answers.toString());
        startActivity(intent);
    }
}

