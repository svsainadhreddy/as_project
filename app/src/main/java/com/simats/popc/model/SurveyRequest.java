package com.simats.popc.model;

import java.util.List;

public class SurveyRequest {
    private int patient_id;
    private int total_score;
    private String status;      // new
    private String risk_level;  // optional
    private List<SectionScore> section_scores;
    private List<Answer> answers;

    public void setPatient_id(int patient_id) { this.patient_id = patient_id; }
    public void setTotal_score(int total_score) { this.total_score = total_score; }
    public void setStatus(String status) { this.status = status; }
    public void setRisk_level(String risk_level) { this.risk_level = risk_level; }
    public void setSection_scores(List<SectionScore> section_scores) { this.section_scores = section_scores; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }

    public static class SectionScore {
        private String section_name;
        private int score;

        public SectionScore(String section_name, int score) {
            this.section_name = section_name;
            this.score = score;
        }
    }

    public static class Answer {
        private String question;
        private String selected_option;
        private int score;
        private String section_name; // ✅ add this


        public Answer(String question, String selected_option, int score,String section_name) {
            this.question = question;
            this.selected_option = selected_option;
            this.score = score;
            this.section_name = section_name;

        }
    }

}
