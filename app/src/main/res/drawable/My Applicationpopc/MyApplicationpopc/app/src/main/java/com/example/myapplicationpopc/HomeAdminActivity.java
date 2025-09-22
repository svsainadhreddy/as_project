package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class HomeAdminActivity extends AppCompatActivity {

    private ImageButton ivBack;
    private TextView tvHome;
    private ImageView hospitalImage;
    private Button btnSearchDoctors, btnAddDoctors;

    // Launcher for AddDoctorActivity
    private final ActivityResultLauncher<Intent> addDoctorLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Forward new doctor data to ListDoctorsActivity
                    Intent intent = new Intent(HomeAdminActivity.this, ListDoctorActivity.class);
                    intent.putExtras(result.getData().getExtras());
                    startActivity(intent);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);

        ivBack = findViewById(R.id.ivBack);
        tvHome = findViewById(R.id.tvHome);
        hospitalImage = findViewById(R.id.hospitalImage);
        btnSearchDoctors = findViewById(R.id.btnSearchDoctors);
        btnAddDoctors = findViewById(R.id.btnAddDoctors);

        // Back → maybe logout or previous
        ivBack.setOnClickListener(v -> finish());

        // Search Doctors → ListDoctorsActivity
        btnSearchDoctors.setOnClickListener(v -> {
            startActivity(new Intent(HomeAdminActivity.this, ListDoctorActivity.class));
        });

        // Add Doctors → AddDoctorActivity
        btnAddDoctors.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, AddDoctorActivity.class);
            addDoctorLauncher.launch(intent);
        });
    }
}
