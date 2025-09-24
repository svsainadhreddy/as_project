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

public class PatientDemographicsActivity extends AppCompatActivity {

    private RadioGroup rgAge, rgSex, rgBmi, rgSmoking, rgAlcohol;
    private Button btnNext;
    private ImageButton btnBack;

    // ✅ Keep patientId as a field
    private int patientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_demographics);

        rgAge = findViewById(R.id.rgAge);
        rgSex = findViewById(R.id.rgSex);
        rgBmi = findViewById(R.id.rgBmi);
        rgSmoking = findViewById(R.id.rgSmoking);
        rgAlcohol = findViewById(R.id.rgAlcohol);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        // ✅ retrieve patient_id safely
        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId <= 0) {
            Toast.makeText(this,
                    "⚠️ Invalid patient ID. Please create a patient first.",
                    Toast.LENGTH_LONG).show();
            // Optional: finish() if you don't want to allow proceeding
        }

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(view -> calculateScoreAndNext());
    }

    private void calculateScoreAndNext() {
        if (patientId <= 0) {
            Toast.makeText(this,
                    "Cannot proceed without a valid patient ID.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        int score = 0;
        JSONObject answers = new JSONObject(); // store question + selected text

        // ---- Age ----
        int ageId = rgAge.getCheckedRadioButtonId();
        if (ageId != -1) {
            RadioButton ageBtn = findViewById(ageId);
            String ageText = ageBtn.getText().toString();
            try { answers.put("Age", ageText); } catch (JSONException ignored) {}
            if (ageText.contains("<50")) score += 0;
            else if (ageText.contains("50")) score += 2;
            else if (ageText.contains("70") || ageText.contains("≥70")) score += 3;
        }

        // ---- Sex ----
        int sexId = rgSex.getCheckedRadioButtonId();
        if (sexId != -1) {
            RadioButton sexBtn = findViewById(sexId);
            try { answers.put("Sex", sexBtn.getText().toString()); } catch (JSONException ignored) {}
        }

        // ---- BMI ----
        int bmiId = rgBmi.getCheckedRadioButtonId();
        if (bmiId != -1) {
            RadioButton bmiBtn = findViewById(bmiId);
            String bmiText = bmiBtn.getText().toString();
            try { answers.put("BMI", bmiText); } catch (JSONException ignored) {}
            if (bmiText.contains("<30")) score += 0;
            else if (bmiText.contains("≥30") || bmiText.contains("30")) score += 2;
        }

        // ---- Smoking ----
        int smokeId = rgSmoking.getCheckedRadioButtonId();
        if (smokeId != -1) {
            RadioButton smokeBtn = findViewById(smokeId);
            String smokeText = smokeBtn.getText().toString();
            try { answers.put("Smoking", smokeText); } catch (JSONException ignored) {}
            if (smokeText.toLowerCase().contains("never")) score += 0;
            else if (smokeText.toLowerCase().contains("ex")) score += 1;
            else if (smokeText.toLowerCase().contains("current")) score += 2;
        }

        // ---- Alcohol ----
        int alcoholId = rgAlcohol.getCheckedRadioButtonId();
        if (alcoholId != -1) {
            RadioButton alcoholBtn = findViewById(alcoholId);
            String alcoholText = alcoholBtn.getText().toString();
            try { answers.put("Alcohol", alcoholText); } catch (JSONException ignored) {}
            if (alcoholText.toLowerCase().contains("no")) score += 0;
            else if (alcoholText.toLowerCase().contains("yes")) score += 1;
        }

        if (answers.length() == 0) {
            Toast.makeText(this,
                    "Please select at least one option",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Section Score: " + score, Toast.LENGTH_SHORT).show();

        // ✅ Always forward the valid patientId
        Intent intent = new Intent(this, MedicalHistoryActivity.class);
        intent.putExtra("patient_score", score);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("patient_demographics_answers", answers.toString());
        startActivity(intent);
    }
}
