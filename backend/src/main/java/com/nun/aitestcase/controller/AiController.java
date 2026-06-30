package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.dto.GenerateTestCasesRequest;
import com.nun.aitestcase.mapper.UserMapper;
import com.nun.aitestcase.service.ai.AiGenerationService;
import com.nun.aitestcase.service.ai.DeepSeekClient;
import com.nun.aitestcase.vo.TestCaseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiGenerationService aiGenerationService;
    private final UserMapper userMapper;

    public AiController(AiGenerationService aiGenerationService, UserMapper userMapper) {
        this.aiGenerationService = aiGenerationService;
        this.userMapper = userMapper;
    }

    @PostMapping("/generate-test-cases")
    public ApiResponse<List<TestCaseVO>> generateTestCases(
            @Valid @RequestBody GenerateTestCasesRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId != null) {
            com.nun.aitestcase.entity.User user = userMapper.selectById(userId);
            if (user != null && user.getApiKey() != null && !user.getApiKey().isBlank()) {
                DeepSeekClient.setUserApiKey(user.getApiKey());
            }
        }
        try {
            return ApiResponse.success(aiGenerationService.generate(request));
        } finally {
            DeepSeekClient.clearUserApiKey();
        }
    }
}
