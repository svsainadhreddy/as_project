package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListDoctorsActivity extends AppCompatActivity {

    private LinearLayout cardsContainer;
    private final ArrayList<String[]> cardData = new ArrayList<>();

    // Launcher for editing doctor
    private final ActivityResultLauncher<Intent> editDoctorLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    int index = data.getIntExtra("index", -1);
                    if (index >= 0) {
                        cardData.set(index, new String[]{
                                data.getStringExtra("id"),
                                data.getStringExtra("name"),
                                data.getStringExtra("gender"),
                                data.getStringExtra("speciality")
                        });
                        refreshCards(); // redraw
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Use the correct doctor layout, not patient layout
        setContentView(R.layout.activity_list_doctor);

        cardsContainer = findViewById(R.id.cards_container);
        EditText searchBar = findViewById(R.id.search_bar);
        ImageButton backButton = findViewById(R.id.btn_back);

        // Back button → HomeAdminActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ListDoctorActivity.this, HomeAdminActivity.class);
            startActivity(intent);
            finish();
        });

        // Default 5 doctors
        cardData.add(new String[]{"201", "Dr. John Smith", "Male", "Cardiology"});
        cardData.add(new String[]{"202", "Dr. Alice Brown", "Female", "Dermatology"});
        cardData.add(new String[]{"203", "Dr. Bob Wilson", "Male", "Neurology"});
        cardData.add(new String[]{"204", "Dr. Mary Johnson", "Female", "Pediatrics"});
        cardData.add(new String[]{"205", "Dr. David Lee", "Male", "Orthopedics"});

        // Check if a new doctor was passed from AddDoctorActivity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            String newId = intent.getStringExtra("id");
            String newName = intent.getStringExtra("name");
            String newGender = intent.getStringExtra("gender");
            String newSpeciality = intent.getStringExtra("speciality");

            cardData.add(new String[]{newId, newName, newGender, newSpeciality});
        }

        // Fill cards
        refreshCards();

        // Search filter
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                for (int i = 0; i < cardsContainer.getChildCount(); i++) {
                    LinearLayout card = (LinearLayout) cardsContainer.getChildAt(i);
                    TextView tvId = card.findViewById(R.id.tv_id);
                    TextView tvName = card.findViewById(R.id.tv_name);

                    String id = tvId.getText().toString().toLowerCase();
                    String name = tvName.getText().toString().toLowerCase();

                    card.setVisibility((id.contains(query) || name.contains(query)) ?
                            LinearLayout.VISIBLE : LinearLayout.GONE);
                }
            }
        });
    }

    // Helper method to refresh all doctor cards
    private void refreshCards() {
        cardsContainer.removeAllViews();

        for (int i = 0; i < cardData.size(); i++) {
            String[] data = cardData.get(i);

            // Inflate card layout dynamically
            LinearLayout card = (LinearLayout) getLayoutInflater()
                    .inflate(R.layout.doctor_card, cardsContainer, false);

            TextView tvId = card.findViewById(R.id.tv_id);
            TextView tvName = card.findViewById(R.id.tv_name);
            TextView tvGender = card.findViewById(R.id.tv_gender);
            TextView tvSpeciality = card.findViewById(R.id.tv_speciality);
            Button btnEdit = card.findViewById(R.id.btn_edit);

            tvId.setText(getString(R.string.doctor_id, data[0]));
            tvName.setText(getString(R.string.doctor_name, data[1]));
            tvGender.setText(getString(R.string.doctor_gender, data[2]));
            tvSpeciality.setText(getString(R.string.doctor_speciality, data[3]));

            int index = i;
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ListDoctorsActivity.this, EditDoctorActivity.class);
                intent.putExtra("index", index);
                intent.putExtra("id", data[0]);
                intent.putExtra("name", data[1]);
                intent.putExtra("gender", data[2]);
                intent.putExtra("speciality", data[3]);
                editDoctorLauncher.launch(intent);
            });

            cardsContainer.addView(card);
        }
    }
}
