// SurveyResponse.java
package com.example.myapplicationpopc.model;

import java.util.List;


// SurveyResponse.java
public class SurveyResponse {
    private int id;
    private int total_score;
    private List<SectionScore> section_scores;
    private List<Answer> answers;

    public int getId(){ return id; }
    public int getTotal_score(){ return total_score; }
    public List<SectionScore> getSection_scores(){ return section_scores; }
    public List<Answer> getAnswers(){ return answers; }

    public static class SectionScore {
        private int id;
        private String section_name;
        public String getSection_name(){ return section_name; }
    }
    public static class Answer {
        private String question_text;
        private String selected_option;
        private int option_score;
        public String getQuestion_text(){ return question_text; }
        public String getSelected_option(){ return selected_option; }
        public int getOption_score(){ return option_score; }
    }
}

