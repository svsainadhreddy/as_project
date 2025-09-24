package com.example.myapplicationpopc.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton to hold all survey data in memory
 * while user navigates through the multi-step form.
 */
public class SurveyData {

    private static SurveyData instance;

    public int patientId;
    public int demographicsScore;
    public int medicalScore;
    public int functionalScore;
    public int surgeryScore;
    public int anesthesiaScore;
    public int postopScore;

    /** List of all question/answer pairs */
    public List<Answer> answers = new ArrayList<>();

    private SurveyData() {}

    public static synchronized SurveyData getInstance() {
        if (instance == null) {
            instance = new SurveyData();
        }
        return instance;
    }

    /** Total of all section scores */
    public int getTotal() {
        return demographicsScore + medicalScore + functionalScore
                + surgeryScore + anesthesiaScore + postopScore;
    }

    /** Clear all stored data when starting a new patient */
    public void clear() {
        patientId = 0;
        demographicsScore = medicalScore = functionalScore =
                surgeryScore = anesthesiaScore = postopScore = 0;
        answers.clear();
    }

    /**
     * Simple POJO to hold each answer.
     * Replace or extend fields if you need more details.
     */
    public static class Answer {
        public String question;
        public String selectedOption;

        public Answer(String question, String selectedOption) {
            this.question = question;
            this.selectedOption = selectedOption;
        }
    }
}
