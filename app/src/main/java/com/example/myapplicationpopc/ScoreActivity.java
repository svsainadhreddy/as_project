package com.example.myapplicationpopc;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ScoreActivity extends AppCompatActivity {

    private int patientId;
    private int patientScore, medicalScore, preopScore,
            surgeryScore, anestheticScore, postopScore, totalScore;

    private TextView tvPatientDemo, tvMedicalHistory, tvPreop,
            tvSurgery, tvAnesthetic, tvPostOp, tvTotalScore, tvManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // Receive scores from Intent
        patientId       = getIntent().getIntExtra("patient_id", -1);
        patientScore    = getIntent().getIntExtra("patient_score", 0);
        medicalScore    = getIntent().getIntExtra("medical_score", 0);
        preopScore      = getIntent().getIntExtra("preop_score", 0);
        surgeryScore    = getIntent().getIntExtra("surgery_score", 0);
        anestheticScore = getIntent().getIntExtra("anesthetic_score", 0);
        postopScore     = getIntent().getIntExtra("postop_score", 0);
        totalScore      = getIntent().getIntExtra("total_score", 0);

        // Bind views
        tvPatientDemo    = findViewById(R.id.itemPatientDemo).findViewById(R.id.tvCategoryScore);
        tvMedicalHistory = findViewById(R.id.itemMedicalHistory).findViewById(R.id.tvCategoryScore);
        tvPreop          = findViewById(R.id.itemPreop).findViewById(R.id.tvCategoryScore);
        tvSurgery        = findViewById(R.id.itemSurgery).findViewById(R.id.tvCategoryScore);
        tvAnesthetic     = findViewById(R.id.itemAnesthetic).findViewById(R.id.tvCategoryScore);
        tvPostOp         = findViewById(R.id.itemPostOp).findViewById(R.id.tvCategoryScore);
        tvTotalScore     = findViewById(R.id.tvTotalScore);
        tvManagement     = findViewById(R.id.tvManagement);

        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> finish());

        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_LONG).show();
            return;
        }

        showScores();
    }

    private void showScores() {
        tvPatientDemo.setText(String.valueOf(patientScore));
        tvMedicalHistory.setText(String.valueOf(medicalScore));
        tvPreop.setText(String.valueOf(preopScore));
        tvSurgery.setText(String.valueOf(surgeryScore));
        tvAnesthetic.setText(String.valueOf(anestheticScore));
        tvPostOp.setText(String.valueOf(postopScore));
        tvTotalScore.setText(String.valueOf(totalScore));

        if (totalScore >= 15) {
            tvManagement.setText("High risk: Needs ICU monitoring");
        } else if (totalScore >= 8) {
            tvManagement.setText("Moderate risk: Close observation");
        } else {
            tvManagement.setText("Low risk: Standard care");
        }
    }
}
