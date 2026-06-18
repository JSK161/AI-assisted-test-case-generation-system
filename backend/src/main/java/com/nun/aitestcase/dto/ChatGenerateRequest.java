package com.nun.aitestcase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ChatGenerateRequest {

    @NotBlank(message = "需求描述不能为空")
    @Size(max = 8000, message = "需求描述不能超过 8000 个字符")
    private String requirement;

    @Valid
    private List<ChatAnswerDTO> answers = new ArrayList<>();

    @Size(max = 1000, message = "参考链接不能超过 1000 个字符")
    private String referenceUrl;

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public List<ChatAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ChatAnswerDTO> answers) {
        this.answers = answers == null ? new ArrayList<>() : answers;
    }

    public String getReferenceUrl() {
        return referenceUrl;
    }

    public void setReferenceUrl(String referenceUrl) {
        this.referenceUrl = referenceUrl;
    }
}
