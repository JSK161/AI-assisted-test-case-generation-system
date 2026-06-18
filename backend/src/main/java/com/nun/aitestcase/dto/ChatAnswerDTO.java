package com.nun.aitestcase.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatAnswerDTO {

    private String questionId;
    private List<String> values = new ArrayList<>();
    private String customText;

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values == null ? new ArrayList<>() : values;
    }

    public String getCustomText() {
        return customText;
    }

    public void setCustomText(String customText) {
        this.customText = customText;
    }
}
