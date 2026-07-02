package com.nun.aitestcase.vo;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationDetailVO {
    private Long id;
    private String title;
    private String requirement;
    private List<MessageVO> messages;
    private Object answers;
    private Object generatedPlan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }
    public List<MessageVO> getMessages() { return messages; }
    public void setMessages(List<MessageVO> messages) { this.messages = messages; }
    public Object getAnswers() { return answers; }
    public void setAnswers(Object answers) { this.answers = answers; }
    public Object getGeneratedPlan() { return generatedPlan; }
    public void setGeneratedPlan(Object generatedPlan) { this.generatedPlan = generatedPlan; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
