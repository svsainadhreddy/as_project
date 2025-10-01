package com.example.myapplicationpopc.model;

import java.util.List;

public class SurveyDisplayResponse {
    private int id;
    private int total_score;
    private String status;
    private List<SectionScore> section_scores;
    private List<Answer> answers;

    public int getId() { return id; }
    public int getTotal_score() { return total_score; }
    public String getStatus() { return status; }
    public List<SectionScore> getSection_scores() { return section_scores; }
    public List<Answer> getAnswers() { return answers; }

    // ---------- Inner Classes ---------- //
    public static class SectionScore {
        private String section;
        private int score;

        public String getSection() { return section; }
        public int getScore() { return score; }
    }

    public static class Answer {
        private String question;
        private String selected_option;
        private String custom_text;
        private int score;

        public String getQuestion() { return question; }
        public String getSelected_option() { return selected_option; }
        public String getCustom_text() { return custom_text; }
        public int getScore() { return score; }
    }
}
