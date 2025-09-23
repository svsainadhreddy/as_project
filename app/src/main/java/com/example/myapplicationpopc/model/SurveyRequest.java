package com.example.myapplicationpopc.model;

import java.util.List;

import java.util.List;

public class SurveyRequest {
    public int patient_id;
    public int total_score;
    public List<SectionScore> section_scores;
    public List<Answer> answers;

    public static class SectionScore {
        public String section;
        public int score;
        public SectionScore(String s, int sc){section=s;score=sc;}
    }
    public static class Answer {
        public String question;
        public String option;
        public int score;
        public Answer(String q,String o,int s){question=q;option=o;score=s;}
    }
}