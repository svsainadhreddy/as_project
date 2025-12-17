package com.simats.popc.model;

import java.util.List;

public class SurveyAnswersResponse {
    public List<AnswerItem> answers;

    public static class AnswerItem {
        public String question;
        public String selected_option;
    }
}
