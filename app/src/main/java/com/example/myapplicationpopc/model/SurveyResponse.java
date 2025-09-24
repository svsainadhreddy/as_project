package com.example.myapplicationpopc.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**  Model for posting survey results to Django API */
public class SurveyResponse {

    @SerializedName("patient_id")
    private int patientId;

    @SerializedName("total_score")
    private int totalScore;

    @SerializedName("recommendation")
    private String recommendation;

    @SerializedName("section_scores")
    private Map<String,Integer> sectionScores;

    // --- getters & setters ----
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public Map<String,Integer> getSectionScores() { return sectionScores; }
    public void setSectionScores(Map<String,Integer> sectionScores) { this.sectionScores = sectionScores; }
}
