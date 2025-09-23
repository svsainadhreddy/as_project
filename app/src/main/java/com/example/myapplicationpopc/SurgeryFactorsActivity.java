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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgery_factors);

        rgSurgeryType = findViewById(R.id.rgSurgeryType);
        rgUrgency     = findViewById(R.id.rgUrgency);
        rgDuration    = findViewById(R.id.rgDuration);
        rgBloodLoss   = findViewById(R.id.rgBloodLoss);
        etOtherSurgery= findViewById(R.id.etOtherSurgery);
        btnNext       = findViewById(R.id.btnNext);
        btnBack       = findViewById(R.id.btnBack);

        // Show text box if "Others" is selected
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
        int totalScore = 0;
        JSONObject answers = new JSONObject();

        // --- Type of surgery ---
        int idType = rgSurgeryType.getCheckedRadioButtonId();
        if (idType != -1) {
            RadioButton rb = findViewById(idType);
            String type = rb.getText().toString();
            String otherTxt = "";
            if (type.equalsIgnoreCase("Others")) {
                otherTxt = etOtherSurgery.getText().toString().trim();
                type = type + (otherTxt.isEmpty() ? "" : " (" + otherTxt + ")");
            }
            try { answers.put("Type of surgery", type); } catch (JSONException ignored) {}

            switch (rb.getText().toString()) {
                case "Thoracic":         totalScore += 7; break;
                case "Upper abdominal":  totalScore += 5; break;
                case "Lower abdominal":  totalScore += 3; break;
                case "Neurosurgery":     totalScore += 3; break;
                case "Orthopedic":       totalScore += 2; break;
                case "Ent / Head & neck":totalScore += 2; break;
                case "Vascular / Cardiac":totalScore += 7; break;
                case "Others":           totalScore += 1; break;
            }
        }

        // --- Urgency ---
        int idUrg = rgUrgency.getCheckedRadioButtonId();
        if (idUrg != -1) {
            RadioButton rb = findViewById(idUrg);
            String urgency = rb.getText().toString();
            try { answers.put("Urgency", urgency); } catch (JSONException ignored) {}
            if (urgency.equalsIgnoreCase("Emergency")) totalScore += 4;
        }

        // --- Duration ---
        int idDur = rgDuration.getCheckedRadioButtonId();
        if (idDur != -1) {
            RadioButton rb = findViewById(idDur);
            String duration = rb.getText().toString();
            try { answers.put("Duration", duration); } catch (JSONException ignored) {}
            if (duration.contains("2–4") || duration.contains("2-4")) totalScore += 3;
            else if (duration.contains(">4") || duration.contains("gt")) totalScore += 5;
        }

        // --- Estimated Blood Loss ---
        int idLoss = rgBloodLoss.getCheckedRadioButtonId();
        if (idLoss != -1) {
            RadioButton rb = findViewById(idLoss);
            String loss = rb.getText().toString();
            try { answers.put("Estimated blood loss", loss); } catch (JSONException ignored) {}
            if (loss.contains("500–1000") || loss.contains("500-1000")) totalScore += 2;
            else if (loss.contains(">1000") || loss.contains("gt")) totalScore += 3;
        }

        Toast.makeText(this, "Surgery Factors Score: " + totalScore, Toast.LENGTH_SHORT).show();

        // Pass score & answers to next activity
        Intent intent = new Intent(this, PlannedAnesthesiaActivity.class); // TODO: replace NextActivity
        intent.putExtra("surgery_score", totalScore);
        intent.putExtra("surgery_answers", answers.toString());
        startActivity(intent);
    }
}
