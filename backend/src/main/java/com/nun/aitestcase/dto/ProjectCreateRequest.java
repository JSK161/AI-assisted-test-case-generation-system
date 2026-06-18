package com.nun.aitestcase.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectCreateRequest {

    @NotBlank(message = "Project name is required")
    private String name;
    private String description;
    private Long creatorId = 1L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
}
