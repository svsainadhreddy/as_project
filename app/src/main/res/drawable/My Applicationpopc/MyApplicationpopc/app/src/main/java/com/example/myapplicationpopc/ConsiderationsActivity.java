package com.example.myapplicationpopc;




import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;



public class ConsiderationsActivity extends AppCompatActivity {

    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_considerations); // your XML filename

        // Initialize Next button
        nextButton = findViewById(R.id.nextButton);

        // Handle click -> move to ScoreActivity
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(ConsiderationsActivity.this, ScoreActivity.class);
            startActivity(intent);
            finish(); // optional: closes current screen
        });
    }
}

