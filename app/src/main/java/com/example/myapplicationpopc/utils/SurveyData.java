package com.example.myapplicationpopc.utils;

import com.example.myapplicationpopc.model.SurveyRequest;

import java.util.ArrayList;
import java.util.List;

public class SurveyData {
    private static SurveyData instance;
    public int patientId;
    public int demographicsScore, medicalScore, functionalScore, surgeryScore, anesthesiaScore, postopScore;
    public List<SurveyRequest.Answer> answers = new ArrayList<>();

    private SurveyData() {
    }

    public static SurveyData getInstance() {
        if (instance == null) instance = new SurveyData();
        return instance;
    }

    public int getTotal() {
        return demographicsScore + medicalScore + functionalScore + surgeryScore + anesthesiaScore + postopScore;
    }
}

