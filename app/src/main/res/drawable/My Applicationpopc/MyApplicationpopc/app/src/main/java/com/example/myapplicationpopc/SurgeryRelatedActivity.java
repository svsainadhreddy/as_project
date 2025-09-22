package com.example.myapplicationpopc;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class SurgeryRelatedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgery_related); // create this XML

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Back → return to PreoperativeSurvey
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SurgeryRelatedActivity.this, PreoperativeSurvey.class);
            startActivity(intent);
            finish();
        });

        // Next → placeholder (for future, or loop back to PreoperativeSurvey)
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            // Example: go back or move forward
            // For now, loop back to PreoperativeSurvey
            Intent intent = new Intent(SurgeryRelatedActivity.this, PlannedAnesthesiaActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
