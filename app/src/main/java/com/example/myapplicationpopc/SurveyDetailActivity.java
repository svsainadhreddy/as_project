package com.example.myapplicationpopc;


import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationpopc.adapter.SectionAdapter;
import com.example.myapplicationpopc.model.SurveyResponse;
import com.example.myapplicationpopc.network.ApiClient;
import com.example.myapplicationpopc.network.ApiService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurveyDetailActivity extends AppCompatActivity {
    RecyclerView recyclerSections;
    TextView tvTitle;
    int patientId;
    String token; // your JWT

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_detail);

        tvTitle = findViewById(R.id.tvTitle);
        recyclerSections = findViewById(R.id.recyclerSections);
        recyclerSections.setLayoutManager(new LinearLayoutManager(this));

        patientId = getIntent().getIntExtra("patient_id",-1);
       // token = "Bearer " + TokenManager.getInstance(this).getAccessToken();
        fetchSurvey();
    }

    private void fetchSurvey() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getSurveyForPatient(token, patientId).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call,
                                   Response<SurveyResponse> response) {
                if(response.isSuccessful() && response.body()!=null){
                    bindData(response.body());
                }else{
                    Toast.makeText(SurveyDetailActivity.this,"No survey", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                Toast.makeText(SurveyDetailActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindData(SurveyResponse survey){
        // group answers by section
        Map<String, List<SurveyResponse.Answer>> map = new LinkedHashMap<>();
        for (SurveyResponse.Answer a : survey.getAnswers()) {
            // you might have section info per answer, if not put all in "General"
            String section = "General";
            if (survey.getSection_scores()!=null && !survey.getSection_scores().isEmpty())
                section = survey.getSection_scores().get(0).getSection_name();
            if (!map.containsKey(section)) map.put(section,new ArrayList<>());
            map.get(section).add(a);
        }

        List<SectionAdapter.SectionData> list = new ArrayList<>();
        for (Map.Entry<String,List<SurveyResponse.Answer>> e : map.entrySet()) {
           // list.add(new SectionAdapter.SectionData(e.getKey(), e.getValue()));
        }

        recyclerSections.setAdapter(new SectionAdapter(list));
    }
}
