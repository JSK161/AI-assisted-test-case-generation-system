package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.dto.AdoptionStatusUpdateRequest;
import com.nun.aitestcase.dto.TestCaseUpdateRequest;
import com.nun.aitestcase.service.TestCaseService;
import com.nun.aitestcase.vo.TestCaseVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test-cases")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @GetMapping("/project/{projectId}")
    public ApiResponse<List<TestCaseVO>> listByProject(@PathVariable Long projectId) {
        return ApiResponse.success(testCaseService.listByProjectId(projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TestCaseVO> getById(@PathVariable Long id) {
        return ApiResponse.success(testCaseService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TestCaseVO> update(
            @PathVariable Long id,
            @Valid @RequestBody TestCaseUpdateRequest request
    ) {
        return ApiResponse.success(testCaseService.update(id, request));
    }

    @PutMapping("/{id}/adoption-status")
    public ApiResponse<TestCaseVO> updateAdoptionStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdoptionStatusUpdateRequest request
    ) {
        return ApiResponse.success(testCaseService.updateAdoptionStatus(id, request.getAdoptionStatus()));
    }
}
