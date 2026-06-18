package com.nun.aitestcase.dto;

import jakarta.validation.constraints.NotNull;

public class GenerateTestCasesRequest {

    @NotNull(message = "Project id is required")
    private Long projectId;

    @NotNull(message = "Requirement id is required")
    private Long requirementId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }
}
