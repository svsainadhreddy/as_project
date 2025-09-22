package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class PlannedAnesthesiaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_anesthesia); // XML you provided

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 🔙 Back button → SurgeryRelatedActivity
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PlannedAnesthesiaActivity.this, SurgeryRelatedActivity.class);
            startActivity(intent);
            finish();
        });


        // ⏭ Next button (you can later connect it to another activity)
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(PlannedAnesthesiaActivity.this, ConsiderationsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
