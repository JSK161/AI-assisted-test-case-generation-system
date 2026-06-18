package com.nun.aitestcase.vo;

import java.util.ArrayList;
import java.util.List;

public class GeneratedQuestionVO {

    private String id;
    private String title;
    private String type;
    private List<GeneratedQuestionOptionVO> options = new ArrayList<>();
    private boolean allowCustom;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<GeneratedQuestionOptionVO> getOptions() {
        return options;
    }

    public void setOptions(List<GeneratedQuestionOptionVO> options) {
        this.options = options == null ? new ArrayList<>() : options;
    }

    public boolean isAllowCustom() {
        return allowCustom;
    }

    public void setAllowCustom(boolean allowCustom) {
        this.allowCustom = allowCustom;
    }
}
