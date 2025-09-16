package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AddDoctorActivity extends AppCompatActivity {

    private EditText etId, etName, etAge, etGender, etPhone, etSpeciality, etUsername, etPassword;
    private Button btnSave;
    private ImageButton ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_doctor); // ✅ reuse your XML

        // Init views
        ivBack = findViewById(R.id.ivBack);
        etId = findViewById(R.id.etId);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etPhone = findViewById(R.id.etPhone);
        etSpeciality = findViewById(R.id.etSpeciality);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);

        // Back button → HomeAdminActivity
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddDoctorActivity.this, HomeAdminActivity.class);
            startActivity(intent);
            finish();
        });

        // Save → Return doctor data
        btnSave.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String gender = etGender.getText().toString().trim();
            String speciality = etSpeciality.getText().toString().trim();

            Intent result = new Intent();
            result.putExtra("id", id);
            result.putExtra("name", name);
            result.putExtra("gender", gender);
            result.putExtra("speciality", speciality);

            setResult(RESULT_OK, result);
            finish(); // go back
        });
    }
}
