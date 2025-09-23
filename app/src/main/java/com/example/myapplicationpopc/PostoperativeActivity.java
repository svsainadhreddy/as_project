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

public class PostoperativeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnNext;

    private RadioGroup groupIcu, groupAnalgesia, groupVentilation, groupMobilization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postoperative);   // XML you posted

        btnBack       = findViewById(R.id.btnBack);
        btnNext       = findViewById(R.id.btnNext);
        groupIcu      = findViewById(R.id.groupIcu);
        groupAnalgesia= findViewById(R.id.groupAnalgesia);
        groupVentilation = findViewById(R.id.groupVentilation);
        groupMobilization = findViewById(R.id.groupMobilization);

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        int totalScore = 0;
        JSONObject answers = new JSONObject();

        // --- ICU/HDU admission ---
        int idIcu = groupIcu.getCheckedRadioButtonId();
        if (idIcu != -1) {
            RadioButton rb = findViewById(idIcu);
            String choice = rb.getText().toString();
            try { answers.put("Planned ICU/HDU admission", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.yes))) {
                totalScore += 2;     // Yes = 2
            }
        }

        // --- Mechanical ventilation >24h ---
        int idVent = groupVentilation.getCheckedRadioButtonId();
        if (idVent != -1) {
            RadioButton rb = findViewById(idVent);
            String choice = rb.getText().toString();
            try { answers.put("Anticipated >24h ventilation", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.yes))) {
                totalScore += 4;     // Yes = 4
            }
        }

        // --- Post-op analgesia ---
        int idAnal = groupAnalgesia.getCheckedRadioButtonId();
        if (idAnal != -1) {
            RadioButton rb = findViewById(idAnal);
            String choice = rb.getText().toString();
            try { answers.put("Post-op analgesia", choice); } catch (JSONException ignored) {}

            // Regional/multimodal = 0, Opioid heavy = 2
            if (choice.equalsIgnoreCase(getString(R.string.opioid_heavy))) {
                totalScore += 2;
            } else {
                totalScore += 0;
            }
        }

        // --- Early mobilization within 24h ---
        int idMob = groupMobilization.getCheckedRadioButtonId();
        if (idMob != -1) {
            RadioButton rb = findViewById(idMob);
            String choice = rb.getText().toString();
            try { answers.put("Early mobilization within 24h", choice); } catch (JSONException ignored) {}
            if (choice.equalsIgnoreCase(getString(R.string.no))) {
                totalScore += 2;     // No = 2
            }
        }

        Toast.makeText(this,
                "Postoperative Score : " + totalScore,
                Toast.LENGTH_LONG).show();

        // ➡️ Pass to next screen or summary
        Intent intent = new Intent(this, DoctorHomeActivity.class); // replace with your next activity
        intent.putExtra("postoperative_score", totalScore);
        intent.putExtra("postoperative_answers", answers.toString());
        startActivity(intent);
    }
}
