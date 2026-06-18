package com.nun.aitestcase.dto;

import jakarta.validation.constraints.NotBlank;

public class AdoptionStatusUpdateRequest {

    @NotBlank(message = "Adoption status is required")
    private String adoptionStatus;

    public String getAdoptionStatus() {
        return adoptionStatus;
    }

    public void setAdoptionStatus(String adoptionStatus) {
        this.adoptionStatus = adoptionStatus;
    }
}
