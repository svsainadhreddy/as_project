package com.simats.popc;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.popc.adapter.ChatAdapter;
import com.simats.popc.model.ChatMessage;
import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PPCChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private TextView tvPatientId;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> messages = new ArrayList<>();

    private ApiService apiService;
    private String token;

    private String patientId = null;
    private boolean isPatientIdSet = false;

    // PID format: PID0001
    private static final Pattern PID_PATTERN =
            Pattern.compile("^PID\\d{4}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Views
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        tvPatientId = findViewById(R.id.tvPatientId);
        ImageButton btnSend = findViewById(R.id.btnSend);

        // RecyclerView
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(messages);
        rvChat.setAdapter(adapter);

        // API
        apiService = ApiClient.getClient().create(ApiService.class);

        String savedToken = SharedPrefManager.getInstance(this).getToken();
        if (savedToken == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        token = "Token " + savedToken;

        // Initial header text
        tvPatientId.setText("PATIENT ID: NOT SET");

        // First AI message
        addAi("Hello. Please enter the Patient ID to begin (e.g., PID0001).");

        btnSend.setOnClickListener(v -> handleUserInput());
    }

    private void handleUserInput() {
        String input = etMessage.getText().toString().trim();
        if (input.isEmpty()) return;

        addUser(input);
        etMessage.setText("");

        // STEP 1: Patient ID not set
        if (!isPatientIdSet) {
            if (PID_PATTERN.matcher(input).matches()) {
                setPatientId(input);
                addAi("Patient ID " + patientId + " confirmed. How can I assist you?");
            } else {
                addAi("Invalid Patient ID format. Please enter like PID0001.");
            }
            return;
        }

        // STEP 2: Change patient ID anytime
        if (PID_PATTERN.matcher(input).matches()) {
            setPatientId(input);
            addAi("Patient ID updated to " + patientId + ". Please continue.");
            return;
        }

        // STEP 3: Normal question flow
        sendQuestionToApi(input);
    }

    // ✅ Centralized Patient ID update
    private void setPatientId(String inputPid) {
        patientId = inputPid.toUpperCase();
        isPatientIdSet = true;
        tvPatientId.setText("PATIENT ID: " + patientId);
    }

    private void sendQuestionToApi(String question) {
        Map<String, Object> body = new HashMap<>();
        body.put("question", question);
        body.put("patient_id", patientId);

        apiService.sendPPCChat(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    addAi(response.body().get("answer").toString());
                } else {
                    addAi("Unable to generate response.");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                addAi("Network error. Please try again.");
            }
        });
    }

    private void addUser(String text) {
        messages.add(new ChatMessage(text, ChatMessage.USER));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    private void addAi(String text) {
        messages.add(new ChatMessage(text, ChatMessage.AI));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }
}
