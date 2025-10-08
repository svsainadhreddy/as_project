package com.example.myapplicationpopc;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.myapplicationpopc.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    RadioGroup rgTheme;
    RadioButton rbLight, rbDark, rbSystem;
    Button btnLanguage, btnSignOut, btnChangePassword;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rgTheme = findViewById(R.id.rgTheme);
        rbLight = findViewById(R.id.rbLight);
        rbDark = findViewById(R.id.rbDark);
        rbSystem = findViewById(R.id.rbSystem);
       // btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // 🔹 Theme Change
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.rbDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });

        // 🔹 Language Selector
        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        // 🔹 Change Password
        btnChangePassword.setOnClickListener(v ->
                Toast.makeText(this, "Change Password screen coming soon", Toast.LENGTH_SHORT).show()
        );

        // 🔹 Sign Out
        btnSignOut.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            Intent intent = new Intent(this, SecondActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language");

        // Create a custom layout for search + list
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText etSearch = new EditText(this);
        etSearch.setHint("Search language...");
        layout.addView(etSearch);

        ListView listView = new ListView(this);
        layout.addView(listView);

        ArrayList<String> languages = new ArrayList<>();
        languages.add("English");
        languages.add("Hindi");
        languages.add("Telugu");
        languages.add("Tamil");
        languages.add("Kannada");
        languages.add("Malayalam");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>(languages));
        listView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<String> filtered = new ArrayList<>();
                for (String lang : languages) {
                    if (lang.toLowerCase().contains(s.toString().toLowerCase())) {
                        filtered.add(lang);
                    }
                }
                listView.setAdapter(new ArrayAdapter<>(SettingsActivity.this,
                        android.R.layout.simple_list_item_1, filtered));
            }
        });

        AlertDialog dialog = builder.setView(layout).create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            setLocale(selected);
            Toast.makeText(this, "Language changed to " + selected, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setLocale(String lang) {
        Locale locale;
        switch (lang.toLowerCase()) {
            case "hindi": locale = new Locale("hi"); break;
            case "telugu": locale = new Locale("te"); break;
            case "tamil": locale = new Locale("ta"); break;
            case "kannada": locale = new Locale("kn"); break;
            case "malayalam": locale = new Locale("ml"); break;
            default: locale = new Locale("en"); break;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
