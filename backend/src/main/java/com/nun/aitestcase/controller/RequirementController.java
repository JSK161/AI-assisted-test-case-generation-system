package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.dto.RequirementCreateRequest;
import com.nun.aitestcase.entity.Requirement;
import com.nun.aitestcase.service.RequirementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/requirements")
public class RequirementController {

    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PostMapping
    public ApiResponse<Requirement> create(@Valid @RequestBody RequirementCreateRequest request) {
        return ApiResponse.success(requirementService.create(request));
    }

    @GetMapping("/project/{projectId}")
    public ApiResponse<List<Requirement>> listByProject(@PathVariable Long projectId) {
        return ApiResponse.success(requirementService.listByProjectId(projectId));
    }
}
