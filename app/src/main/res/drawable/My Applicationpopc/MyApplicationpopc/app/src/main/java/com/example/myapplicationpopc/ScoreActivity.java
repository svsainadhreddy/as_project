package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ScoreActivity extends AppCompatActivity {

    private TextView textDemoScore, textMedicalScore, textPreopScore,
            textSurgeryScore, textAnestheticScore, textPostopScore,
            textTotalScore, textManage;

    private Button doneButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score); // link XML file

        // Initialize views
        textDemoScore = findViewById(R.id.textDemoScore);
        textMedicalScore = findViewById(R.id.textMedicalScore);
        textPreopScore = findViewById(R.id.textPreopScore);
        textSurgeryScore = findViewById(R.id.textSurgeryScore);
        textAnestheticScore = findViewById(R.id.textAnestheticScore);
        textPostopScore = findViewById(R.id.textPostopScore);
        textTotalScore = findViewById(R.id.textTotalScore);
        textManage = findViewById(R.id.textManage);

        doneButton = findViewById(R.id.doneButton);
        backButton = findViewById(R.id.backButton);

        // Get data from Intent
        Intent intent = getIntent();
        int demoScore = intent.getIntExtra("demo_score", 0);
        int medicalScore = intent.getIntExtra("medical_score", 0);
        int preopScore = intent.getIntExtra("preop_score", 0);
        int surgeryScore = intent.getIntExtra("surgery_score", 0);
        int anestheticScore = intent.getIntExtra("anesthetic_score", 0);
        int postopScore = intent.getIntExtra("postop_score", 0);

        // Calculate total
        int totalScore = demoScore + medicalScore + preopScore + surgeryScore + anestheticScore + postopScore;

        // Set values in UI
        textDemoScore.setText(String.valueOf(demoScore));
        textMedicalScore.setText(String.valueOf(medicalScore));
        textPreopScore.setText(String.valueOf(preopScore));
        textSurgeryScore.setText(String.valueOf(surgeryScore));
        textAnestheticScore.setText(String.valueOf(anestheticScore));
        textPostopScore.setText(String.valueOf(postopScore));
        textTotalScore.setText(String.valueOf(totalScore));

        // Management advice
        textManage.setText(getManagementAdvice(totalScore));

        // ✅ Done button → navigate to DoctorHomeActivity
        doneButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(ScoreActivity.this, DoctorHomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });

        // ✅ Back button → just go back
        backButton.setOnClickListener(v -> finish());
    }

    private String getManagementAdvice(int score) {
        if (score < 20) {
            return "Low risk: Standard monitoring and routine care.";
        } else if (score < 40) {
            return "Moderate risk: Consider optimization pre-op and close post-op monitoring.";
        } else if (score < 60) {
            return "High risk: Requires optimization, consider ICU planning, careful anesthesia.";
        } else {
            return "Very high risk: Strongly consider avoiding GA/ETT if possible; optimize comorbidities pre-op, mandatory ICU planning.";
        }
    }
}
