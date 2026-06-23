package com.nun.aitestcase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QuestionGenerateRequest {

    @NotBlank(message = "需求描述不能为空")
    @Size(max = 8000, message = "需求描述不能超过 8000 个字符")
    private String requirement;

    @Size(max = 1000, message = "参考链接不能超过 1000 个字符")
    private String referenceUrl;

    private Long conversationId;

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getReferenceUrl() {
        return referenceUrl;
    }

    public void setReferenceUrl(String referenceUrl) {
        this.referenceUrl = referenceUrl;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}
