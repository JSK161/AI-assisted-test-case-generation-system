package com.nun.aitestcase.vo;

import java.util.ArrayList;
import java.util.List;

public class GeneratedQuestionResponseVO {

    private List<GeneratedQuestionVO> questions = new ArrayList<>();
    private String usedModel;

    public List<GeneratedQuestionVO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<GeneratedQuestionVO> questions) {
        this.questions = questions == null ? new ArrayList<>() : questions;
    }

    public String getUsedModel() {
        return usedModel;
    }

    public void setUsedModel(String usedModel) {
        this.usedModel = usedModel;
    }
}
