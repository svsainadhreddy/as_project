package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class PreoperativeSurvey extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preoperative_survey);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Back → go to MedicalHistoryActivity
        ImageButton backButton = findViewById(R.id.ivBack);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PreoperativeSurvey.this, MedicalHistoryActivity.class);
            startActivity(intent);
            finish();
        });

        // Next → go to SurgeryRelatedActivity
        Button nextButton = findViewById(R.id.btnNext);
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(PreoperativeSurvey.this, SurgeryRelatedActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
