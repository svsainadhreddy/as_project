package com.example.myapplicationpopc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ListPatientActivity extends AppCompatActivity {

    private LinearLayout cardsContainer;
    private static final int EDIT_REQUEST_CODE = 1;

    // Dummy data for 5 cards
    private final String[][] cardData = {
            {"101", "John Doe", "Male", "1234567890"},
            {"102", "Alice Smith", "Female", "9876543210"},
            {"103", "Bob Johnson", "Male", "4561237890"},
            {"104", "Mary Jane", "Female", "7418529630"},
            {"105", "David Lee", "Male", "8529637410"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_patient);

        cardsContainer = findViewById(R.id.cards_container);
        EditText searchBar = findViewById(R.id.search_bar);
        Button backButton = findViewById(R.id.btn_back);

        // Back to DoctorHomeActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ListPatientActivity.this, DoctorHomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Fill cards with data
        for (int i = 0; i < cardsContainer.getChildCount(); i++) {
            LinearLayout card = (LinearLayout) cardsContainer.getChildAt(i);

            TextView tvId = card.findViewById(R.id.tv_id);
            TextView tvName = card.findViewById(R.id.tv_name);
            TextView tvGender = card.findViewById(R.id.tv_gender);
            TextView tvPhone = card.findViewById(R.id.tv_phone);
            Button btnEdit = card.findViewById(R.id.btn_edit);

            String[] data = cardData[i];
            tvId.setText(getString(R.string.label_id, data[0]));
            tvName.setText(getString(R.string.label_name, data[1]));
            tvGender.setText(getString(R.string.label_gender, data[2]));
            tvPhone.setText(getString(R.string.label_phone, data[3]));

            int index = i; // capture for lambda
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ListPatientActivity.this, AddDoctorActivity.class);
                intent.putExtra("index", index);
                intent.putExtra("id", data[0]);
                intent.putExtra("name", data[1]);
                intent.putExtra("gender", data[2]);
                intent.putExtra("phone", data[3]);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
            });
        }

        // Search function
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int index = data.getIntExtra("index", -1);
            if (index >= 0) {
                cardData[index][0] = data.getStringExtra("id");
                cardData[index][1] = data.getStringExtra("name");
                cardData[index][2] = data.getStringExtra("gender");
                cardData[index][3] = data.getStringExtra("phone");

                // Refresh UI
                LinearLayout card = (LinearLayout) cardsContainer.getChildAt(index);
                ((TextView) card.findViewById(R.id.tv_id)).setText(getString(R.string.label_id, cardData[index][0]));
                ((TextView) card.findViewById(R.id.tv_name)).setText(getString(R.string.label_name, cardData[index][1]));
                ((TextView) card.findViewById(R.id.tv_gender)).setText(getString(R.string.label_gender, cardData[index][2]));
                ((TextView) card.findViewById(R.id.tv_phone)).setText(getString(R.string.label_phone, cardData[index][3]));
            }
        }
    }
}
