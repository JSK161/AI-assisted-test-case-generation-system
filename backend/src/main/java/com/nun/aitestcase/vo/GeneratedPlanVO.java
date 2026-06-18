package com.nun.aitestcase.vo;

import java.util.ArrayList;
import java.util.List;

public class GeneratedPlanVO {

    private String title;
    private String summary;
    private List<String> scope = new ArrayList<>();
    private List<String> risks = new ArrayList<>();
    private List<TestCasePlanItemVO> testCases = new ArrayList<>();
    private String usedModel;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope == null ? new ArrayList<>() : scope;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks == null ? new ArrayList<>() : risks;
    }

    public List<TestCasePlanItemVO> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCasePlanItemVO> testCases) {
        this.testCases = testCases == null ? new ArrayList<>() : testCases;
    }

    public String getUsedModel() {
        return usedModel;
    }

    public void setUsedModel(String usedModel) {
        this.usedModel = usedModel;
    }
}
