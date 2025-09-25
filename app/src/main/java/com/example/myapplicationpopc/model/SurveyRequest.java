package com.example.myapplicationpopc.model;

import java.util.List;

public class SurveyRequest {
    private int patient_id;
    private int total_score;
    private List<SectionScore> section_scores; // optional
    private List<Answer> answers;

    // constructors, getters, setters

    public static class SectionScore {
        private String section_name;
        private int section_score;

        public SectionScore() {}
        public SectionScore(String name, int score) {
            this.section_name = name;
            this.section_score = score;
        }
        // getters & setters
        public String getSection_name() { return section_name; }
        public void setSection_name(String section_name) { this.section_name = section_name; }
        public int getSection_score() { return section_score; }
        public void setSection_score(int section_score) { this.section_score = section_score; }
    }

    public static class Answer {
        private String question_text;
        private String selected_option;
        private int option_score;

        public Answer() {}
        public Answer(String question_text, String selected_option, int option_score) {
            this.question_text = question_text;
            this.selected_option = selected_option;
            this.option_score = option_score;
        }
        // getters & setters
        public String getQuestion_text() { return question_text; }
        public void setQuestion_text(String question_text) { this.question_text = question_text; }
        public String getSelected_option() { return selected_option; }
        public void setSelected_option(String selected_option) { this.selected_option = selected_option; }
        public int getOption_score() { return option_score; }
        public void setOption_score(int option_score) { this.option_score = option_score; }
    }

    // getters and setters for fields
    public int getPatient_id() { return patient_id; }
    public void setPatient_id(int patient_id) { this.patient_id = patient_id; }
    public int getTotal_score() { return total_score; }
    public void setTotal_score(int total_score) { this.total_score = total_score; }
    public List<SectionScore> getSection_scores() { return section_scores; }
    public void setSection_scores(List<SectionScore> section_scores) { this.section_scores = section_scores; }
    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }
}
