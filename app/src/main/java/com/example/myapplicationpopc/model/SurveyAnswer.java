package com.example.myapplicationpopc.model;

public class SurveyAnswer {
    public String section;
    public String question;
    public String selected_option;
    public int score;

    public SurveyAnswer(String section, String question,
                        String selected_option, int score) {
        this.section = section;
        this.question = question;
        this.selected_option = selected_option;
        this.score = score;
    }
}
