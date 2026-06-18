package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.dto.GenerateTestCasesRequest;
import com.nun.aitestcase.service.ai.AiGenerationService;
import com.nun.aitestcase.vo.TestCaseVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiGenerationService aiGenerationService;

    public AiController(AiGenerationService aiGenerationService) {
        this.aiGenerationService = aiGenerationService;
    }

    @PostMapping("/generate-test-cases")
    public ApiResponse<List<TestCaseVO>> generateTestCases(@Valid @RequestBody GenerateTestCasesRequest request) {
        return ApiResponse.success(aiGenerationService.generate(request));
    }
}
