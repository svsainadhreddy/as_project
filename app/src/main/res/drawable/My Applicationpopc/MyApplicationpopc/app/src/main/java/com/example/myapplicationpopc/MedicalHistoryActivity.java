package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

public class MedicalHistoryActivity extends AppCompatActivity {

    private Button nextButton;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide default toolbar
        }

        // Back button → PatientDemographicActivity
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MedicalHistoryActivity.this, PatientDemographicActivity.class);
            startActivity(intent);
            finish();
        });

        // ScrollView and Next Button
        scrollView = findViewById(R.id.scrollView);
        nextButton = findViewById(R.id.nextButton);

        // Initially hide Next button
        nextButton.setVisibility(View.GONE);

        // Show Next button when scrolled to bottom
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View view = scrollView.getChildAt(scrollView.getChildCount() - 1);

            int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

            if (diff <= 0) {
                nextButton.setVisibility(View.VISIBLE);
            }
        });

        // Next button → PreoperativeFunctionalActivity
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(MedicalHistoryActivity.this, PreoperativeSurvey.class);
            startActivity(intent);
            finish();
        });
    }
}
