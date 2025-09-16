package com.example.myapplicationpopc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DoctorProfileActivity extends AppCompatActivity {

    private TextView tvId, tvName, tvPhone, tvEmail, tvSpec;
    private EditText etId, etName, etPhone, etEmail, etSpec;
    private Button editButton, saveButton;
    private ImageView ivBack;

    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        // Initialize views
        tvId = findViewById(R.id.tvId);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvSpec = findViewById(R.id.tvSpec);

        etId = findViewById(R.id.etId);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etSpec = findViewById(R.id.etSpec);

        editButton = findViewById(R.id.editButton);
        saveButton = new Button(this);
        saveButton.setText(getString(R.string.save));
        saveButton.setVisibility(View.GONE);

        ((android.widget.LinearLayout) findViewById(R.id.bottomContainer)).addView(saveButton);

        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> onBackPressed());

        // Initially set profile values
        tvId.setText(getString(R.string.label_id, "101"));
        tvName.setText(getString(R.string.label_name, "John Doe"));
        tvPhone.setText(getString(R.string.label_phone, "9876543210"));
        tvEmail.setText(getString(R.string.label_email, "doctor@gmail.com"));
        tvSpec.setText(getString(R.string.label_specialization, "Cardiologist"));

        // Edit button logic
        editButton.setOnClickListener(v -> toggleEditMode());

        // Save button logic
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void toggleEditMode() {
        if (!isEditing) {
            // Switch to edit mode
            isEditing = true;
            editButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);

            // Transfer values from TextViews to EditTexts
            etId.setText(tvId.getText().toString().replace("Id : ", ""));
            etName.setText(tvName.getText().toString().replace("Name : ", ""));
            etPhone.setText(tvPhone.getText().toString().replace("Phn.no : ", ""));
            etEmail.setText(tvEmail.getText().toString().replace("E-mail : ", ""));
            etSpec.setText(tvSpec.getText().toString().replace("Specialization : ", ""));

            // Show EditTexts
            etId.setVisibility(View.VISIBLE);
            etName.setVisibility(View.VISIBLE);
            etPhone.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.VISIBLE);
            etSpec.setVisibility(View.VISIBLE);

            // Hide TextViews
            tvId.setVisibility(View.GONE);
            tvName.setVisibility(View.GONE);
            tvPhone.setVisibility(View.GONE);
            tvEmail.setVisibility(View.GONE);
            tvSpec.setVisibility(View.GONE);

        }
    }

    private void saveProfile() {
        // Switch back to view mode
        isEditing = false;
        editButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);

        // Save data back into TextViews
        tvId.setText(getString(R.string.label_id, etId.getText().toString()));
        tvName.setText(getString(R.string.label_name, etName.getText().toString()));
        tvPhone.setText(getString(R.string.label_phone, etPhone.getText().toString()));
        tvEmail.setText(getString(R.string.label_email, etEmail.getText().toString()));
        tvSpec.setText(getString(R.string.label_specialization, etSpec.getText().toString()));

        // Hide EditTexts
        etId.setVisibility(View.GONE);
        etName.setVisibility(View.GONE);
        etPhone.setVisibility(View.GONE);
        etEmail.setVisibility(View.GONE);
        etSpec.setVisibility(View.GONE);

        // Show TextViews
        tvId.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.VISIBLE);
        tvPhone.setVisibility(View.VISIBLE);
        tvEmail.setVisibility(View.VISIBLE);
        tvSpec.setVisibility(View.VISIBLE);
    }
}
