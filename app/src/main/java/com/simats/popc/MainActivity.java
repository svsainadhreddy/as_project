package com.simats.popc;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.simats.popc.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPrefManager sp = SharedPrefManager.getInstance(this);

        // 🔐 Single source of truth
        if (sp.isLoggedIn()) {
            startActivity(new Intent(this, DoctorHomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        AppCompatButton btnLogin = findViewById(R.id.btnLogin);
        AppCompatButton btnRegister = findViewById(R.id.btnSignin);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, SecondActivity.class)));

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
