package com.example.myapplicationpopc.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Exact payload required by Django SurveySerializer */
public class SurveyRequest {

    @SerializedName("patient_id")
    private int patientId;

    @SerializedName("total_score")
    private int totalScore;

    @SerializedName("risk_level")
    private String riskLevel;

    @SerializedName("recommendation")
    private String recommendation;

    @SerializedName("sections")
    private List<Section> sections;

    // ---- setters ----
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public void setSections(List<Section> sections) { this.sections = sections; }

    // ===== Nested classes =====
    public static class Section {
        private String name;
        private int score;
        private List<Answer> answers;

        public Section(String name, int score, List<Answer> answers) {
            this.name = name;
            this.score = score;
            this.answers = answers;
        }
    }

    public static class Answer {
        @SerializedName("question_id")
        private int questionId;
        @SerializedName("selected_option")
        private String selectedOption;
        private int score;

        public Answer(int questionId, String selectedOption, int score) {
            this.questionId = questionId;
            this.selectedOption = selectedOption;
            this.score = score;
        }
    }
}
