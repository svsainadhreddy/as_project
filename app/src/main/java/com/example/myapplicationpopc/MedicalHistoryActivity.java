package com.example.myapplicationpopc;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class MedicalHistoryActivity extends AppCompatActivity {

    private RadioGroup rgCOPD, rgAsthma, rgOSA, rgILD, rgHeartFailure,
            rgCAD, rgHypertension, rgDiabetes, rgCKD;
    private Button btnNext;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);

        // --- find views ---
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        rgCOPD        = findViewById(R.id.rgCOPD);
        rgAsthma      = findViewById(R.id.rgAsthma);
        rgOSA         = findViewById(R.id.rgOSA);
        rgILD         = findViewById(R.id.rgILD);
        rgHeartFailure= findViewById(R.id.rgHeartFailure);
        rgCAD         = findViewById(R.id.rgCAD);
        rgHypertension= findViewById(R.id.rgHypertension);
        rgDiabetes    = findViewById(R.id.rgDiabetes);
        rgCKD         = findViewById(R.id.rgCKD);

        btnBack.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        int totalScore = 0;
        JSONObject answers = new JSONObject();

        // Helper function
        totalScore += addDiseaseScore(rgCOPD,        "COPD", 3, answers);
        totalScore += addDiseaseScore(rgAsthma,      "Asthma", 2, answers);
        totalScore += addDiseaseScore(rgOSA,         "Obstructive Sleep Apnea", 2, answers);
        totalScore += addDiseaseScore(rgILD,         "Interstitial Lung Disease", 3, answers);
        totalScore += addDiseaseScore(rgHeartFailure,"Heart Failure", 2, answers);
        totalScore += addDiseaseScore(rgCAD,         "CAD / Recent MI", 2, answers);
        totalScore += addDiseaseScore(rgHypertension,"Hypertension", 1, answers);
        totalScore += addDiseaseScore(rgDiabetes,    "Diabetes", 1, answers);
        totalScore += addDiseaseScore(rgCKD,         "Chronic Kidney Disease", 2, answers);

        // Cap at 15 points
        if (totalScore > 15) totalScore = 15;

        Toast.makeText(this,
                "Medical History Score: " + totalScore,
                Toast.LENGTH_SHORT).show();

        // Pass to next screen
        Intent intent = new Intent(this, PreoperativeConsiderationsActivity.class);
        intent.putExtra("medical_history_score", totalScore);
        intent.putExtra("medical_history_answers", answers.toString());
        startActivity(intent);
    }

    private int addDiseaseScore(RadioGroup group, String key, int weight, JSONObject answers) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == -1) return 0; // nothing selected

        RadioButton rb = findViewById(checkedId);
        String ans = rb.getText().toString();

        try { answers.put(key, ans); } catch (JSONException ignored) {}

        return ans.equalsIgnoreCase("Yes") ? weight : 0;
    }
}

